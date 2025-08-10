package com.eric.securechat.friendship.application;

import com.eric.securechat.friendship.dto.FriendRequestViewDto;
import com.eric.securechat.friendship.dto.FriendStatusDto;
import com.eric.securechat.friendship.domain.Friendship;
import com.eric.securechat.friendship.domain.FriendshipId;
import com.eric.securechat.friendship.domain.FriendshipStatus;
import com.eric.securechat.user.domain.User;
import com.eric.securechat.friendship.repository.FriendshipRepository;
import com.eric.securechat.message.repository.MessageRepository;
import com.eric.securechat.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of FriendshipService for managing user friendship relationships.
 * Provides comprehensive functionality for friend requests, status management,
 * and relationship queries with proper validation and security checks.
 */
@Service
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageRepository messageRepository;

    /**
     * Constructor for FriendshipServiceImpl.
     *
     * @param userRepository         Repository for user data operations
     * @param friendshipRepository   Repository for friendship data operations
     * @param messageRepository      Repository for message data operations
     */
    public FriendshipServiceImpl(UserRepository userRepository, FriendshipRepository friendshipRepository, @Lazy MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Sends a friend request from one user to another.
     * Validates that users exist, checks for existing relationships,
     * and prevents self-requests and blocked user interactions.
     *
     * @param requesterUsername The username of the user sending the request
     * @param addresseeUsername The username of the user receiving the request
     * @return The created friendship record
     * @throws Exception if validation fails or relationship already exists
     */
    @Override
    @Transactional
    public Friendship sendRequest(String requesterUsername, String addresseeUsername) throws Exception {
        log.info("Processing friend request from '{}' to '{}'", requesterUsername, addresseeUsername);

        if (requesterUsername.equals(addresseeUsername)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(addresseeUsername);

        Optional<Friendship> existingFriendshipOpt = findFriendshipRelation(requester, addressee);

        if (existingFriendshipOpt.isPresent()) {
            Friendship existingFriendship = existingFriendshipOpt.get();
            FriendshipStatus status = existingFriendship.getStatus();
            log.warn("Cannot send request from '{}' to '{}'. An existing relationship was found with status: {}",
                    requesterUsername, addresseeUsername, status);

            if (status == FriendshipStatus.BLOCKED) {
                if (existingFriendship.getActionUser() != null && existingFriendship.getActionUser().equals(addressee)) {
                    throw new IllegalStateException("You are blocked by this user and cannot send a friend request.");
                } else {
                    throw new IllegalStateException("You have blocked this user. Please unblock them to send a request.");
                }
            }
            if (status == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("You are already friends.");
            }
            throw new IllegalStateException("A friend request already exists between you two.");
        }

        Friendship friendship = new Friendship(requester, addressee);
        friendship.setActionUser(requester);

        log.info("Friend request from '{}' to '{}' created successfully.", requesterUsername, addresseeUsername);
        return friendshipRepository.save(friendship);
    }

    /**
     * Accepts a friend request from another user.
     * Validates the request exists and is in pending status.
     *
     * @param requesterUsername The username of the user who sent the original request
     * @param currentUserUsername The username of the current user accepting the request
     * @return The updated friendship record
     * @throws Exception if request doesn't exist or status is incorrect
     */
    @Override
    @Transactional
    public Friendship acceptRequest(String requesterUsername, String currentUserUsername) throws Exception {
        log.info("User '{}' is attempting to accept a friend request from '{}'.", currentUserUsername, requesterUsername);

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(currentUserUsername);

        FriendshipId friendshipId = new FriendshipId(requester.getId(), addressee.getId());
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalStateException("Friend request from '" + requesterUsername + "' not found."));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("This friend request is not pending. Current status: " + friendship.getStatus());
        }
        if (!friendship.getAddressee().equals(addressee)) {
            throw new SecurityException("You are not authorized to accept this request.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setActionUser(addressee);

        log.info("User '{}' successfully accepted the friend request from '{}'.", currentUserUsername, requesterUsername);
        return friendshipRepository.save(friendship);
    }

    /**
     * Declines a friend request from another user.
     * Removes the friendship record and returns a transient object for compatibility.
     *
     * @param requesterUsername The username of the user who sent the original request
     * @param currentUserUsername The username of the current user declining the request
     * @return The deleted friendship record with DECLINED status
     * @throws Exception if request doesn't exist or status is incorrect
     */
    @Override
    @Transactional
    public Friendship declineRequest(String requesterUsername, String currentUserUsername) throws Exception {
        log.info("User '{}' is attempting to decline a friend request from '{}'.", currentUserUsername, requesterUsername);

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(currentUserUsername);

        FriendshipId friendshipId = new FriendshipId(requester.getId(), addressee.getId());
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalStateException("Friend request from '" + requesterUsername + "' not found."));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("This request is not pending.");
        }
        if (!friendship.getAddressee().equals(addressee)) {
            throw new SecurityException("You are not authorized to decline this request.");
        }

        friendshipRepository.delete(friendship);
        log.info("Friend request from '{}' was declined and removed by '{}'.", requesterUsername, currentUserUsername);

        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendship;
    }

    /**
     * Removes a user from the current user's friend list.
     * Only works with accepted friendships.
     *
     * @param currentUsername The current user's username
     * @param friendUsername The friend's username to remove
     * @return The deleted friendship record
     * @throws Exception if friendship doesn't exist or cannot be removed
     */
    @Override
    @Transactional
    public Friendship unfriend(String currentUsername, String friendUsername) throws Exception {
        log.info("User '{}' is attempting to unfriend '{}'.", currentUsername, friendUsername);

        User currentUser = findUserByUsername(currentUsername);
        User friendToUnfriend = findUserByUsername(friendUsername);

        Friendship friendship = findFriendshipRelation(currentUser, friendToUnfriend)
                .orElseThrow(() -> new IllegalStateException("Friendship record not found between " + currentUsername + " and " + friendUsername));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            log.warn("Unfriend attempt failed. Users '{}' and '{}' are not friends. Current status: {}", currentUsername, friendUsername, friendship.getStatus());
            throw new IllegalStateException("You can only unfriend someone who is currently your friend.");
        }

        // Delete the conversation history
        messageRepository.deleteConversation(currentUser.getId(), friendToUnfriend.getId());

        friendshipRepository.delete(friendship);
        log.info("User '{}' successfully unfriended '{}'.", currentUsername, friendUsername);

        return friendship;
    }

    /**
     * Blocks a user, preventing any interaction between the users.
     * Creates or updates friendship status to BLOCKED.
     *
     * @param blockerUsername The username of the user doing the blocking
     * @param blockedUsername The username of the user being blocked
     * @return The updated or created friendship record
     */
    @Override
    @Transactional
    public Friendship blockUser(String blockerUsername, String blockedUsername) {
        log.info("User '{}' is attempting to block user '{}'.", blockerUsername, blockedUsername);

        User blocker = findUserByUsername(blockerUsername);
        User blocked = findUserByUsername(blockedUsername);

        Optional<Friendship> friendshipOpt = findFriendshipRelation(blocker, blocked);
        Friendship friendship;

        if (friendshipOpt.isPresent()) {
            log.info("Found existing relationship between '{}' and '{}'. Updating status to BLOCKED.", blockerUsername, blockedUsername);
            friendship = friendshipOpt.get();
        } else {
            log.info("No existing relationship found. Creating new BLOCKED relationship for '{}' and '{}'.", blockerUsername, blockedUsername);
            User requester, addressee;
            if (blocker.getId().toString().compareTo(blocked.getId().toString()) < 0) {
                requester = blocker;
                addressee = blocked;
            } else {
                requester = blocked;
                addressee = blocker;
            }
            friendship = new Friendship(requester, addressee);
        }

        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setActionUser(blocker);

        return friendshipRepository.save(friendship);
    }

    /**
     * Retrieves the friend list for a user with friendship status.
     * Returns both accepted friends and blocked users with their status.
     *
     * @param username The username to get friends for
     * @return List of friends with their status and cryptographic keys
     */
    @Override
    public List<FriendStatusDto> getFriendsList(String username) {
        log.debug("Fetching friend and blocked list for user '{}'", username);
        User currentUser = findUserByUsername(username);

        List<Friendship> acceptedFriendships = friendshipRepository.findAllByUserAndStatus(currentUser, FriendshipStatus.ACCEPTED);
        List<Friendship> blockedFriendships = friendshipRepository.findAllByUserAndStatus(currentUser, FriendshipStatus.BLOCKED);

        List<Friendship> allRelations = Stream.concat(acceptedFriendships.stream(), blockedFriendships.stream())
                .collect(Collectors.toList());

        return allRelations.stream()
                .map(friendship -> {
                    User otherUser = friendship.getRequester().equals(currentUser) ? friendship.getAddressee() : friendship.getRequester();
                    return new FriendStatusDto(
                            otherUser.getId(),
                            otherUser.getUsername(),
                            otherUser.getNickname(),
                            friendship.getStatus(),
                            otherUser.getEd25519PublicKey(),
                            otherUser.getX25519PublicKey()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all pending friend requests for the current user.
     *
     * @param currentUsername The username of the current logged-in user
     * @return List of pending friend request DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestViewDto> getPendingRequests(String currentUsername) {
        log.debug("Fetching pending friend requests for user '{}'", currentUsername);
        User currentUser = findUserByUsername(currentUsername);

        List<Friendship> pendingFriendships = friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING);

        return pendingFriendships.stream()
                .map(friendship -> {
                    User requester = friendship.getRequester();
                    return new FriendRequestViewDto(
                            requester.getId(),
                            requester.getUsername(),
                            requester.getNickname(),
                            friendship.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Unblocks a previously blocked user.
     * Only the user who performed the blocking can unblock.
     *
     * @param currentUserUsername The username of the user performing the unblock operation
     * @param blockedUsername The username of the user being unblocked
     * @return The updated friendship record
     * @throws Exception if relationship doesn't exist, status is incorrect, or no permission
     */
    @Override
    @Transactional
    public Friendship unblockUser(String currentUserUsername, String blockedUsername) throws Exception {
        log.info("User '{}' is attempting to unblock user '{}'.", currentUserUsername, blockedUsername);

        User currentUser = findUserByUsername(currentUserUsername);
        User userToUnblock = findUserByUsername(blockedUsername);

        Friendship friendship = findFriendshipRelation(currentUser, userToUnblock)
                .orElseThrow(() -> new IllegalStateException("No relationship record found with user: " + blockedUsername));

        if (friendship.getStatus() != FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("This user is not blocked.");
        }

        if (friendship.getActionUser() == null || !friendship.getActionUser().equals(currentUser)) {
            log.warn("Security check failed: User '{}' tried to unblock '{}', but the original blocker was '{}'.",
                    currentUserUsername, blockedUsername,
                    friendship.getActionUser() != null ? friendship.getActionUser().getUsername() : "unknown");
            throw new SecurityException("Only the user who initiated the block can unblock.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setActionUser(null);
        Friendship updatedFriendship = friendshipRepository.save(friendship);

        log.info("User '{}' successfully unblocked user '{}'. Their friendship status has been restored to ACCEPTED.",
                currentUserUsername, blockedUsername);

        return updatedFriendship;
    }

    /**
     * Finds friendship relationship between two users.
     * Uses bidirectional query to check both directions.
     *
     * @param user1 The first user
     * @param user2 The second user
     * @return Optional containing the friendship if it exists, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Friendship> findFriendshipRelation(User user1, User user2) {
        return friendshipRepository.findFriendshipBetweenUsers(user1, user2);
    }

    /**
     * Helper method to find user by username with unified error handling.
     *
     * @param username The username to search for
     * @return User entity
     * @throws UsernameNotFoundException if user is not found
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
