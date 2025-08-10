package com.eric.securechat.friendship.application;

import com.eric.securechat.friendship.dto.FriendRequestViewDto;
import com.eric.securechat.friendship.dto.FriendStatusDto;
import com.eric.securechat.friendship.domain.Friendship;
import com.eric.securechat.user.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing friendship relationships between users.
 * Provides functionality for friend requests, status management, and relationship queries.
 */
public interface FriendshipService {

    /**
     * Retrieves all pending friend requests for the current user.
     * 
     * @param currentUsername The username of the current logged-in user
     * @return List of pending friend request DTOs
     */
    List<FriendRequestViewDto> getPendingRequests(String currentUsername);

    /**
     * Sends a friend request from one user to another.
     * 
     * @param requesterUsername The username of the user sending the request
     * @param addresseeUsername The username of the user receiving the request
     * @return The created friendship record
     * @throws Exception if users don't exist, are already friends, or request already exists
     */
    Friendship sendRequest(String requesterUsername, String addresseeUsername) throws Exception;

    /**
     * Accepts a friend request.
     * 
     * @param requesterUsername The username of the user who sent the original request
     * @param addresseeUsername The username of the current user accepting the request
     * @return The updated friendship record
     * @throws Exception if request doesn't exist or status is incorrect
     */
    Friendship acceptRequest(String requesterUsername, String addresseeUsername) throws Exception;

    /**
     * Declines a friend request.
     * 
     * @param requesterUsername The username of the user who sent the original request
     * @param addresseeUsername The username of the current user declining the request
     * @return The updated friendship record
     * @throws Exception if request doesn't exist or status is incorrect
     */
    Friendship declineRequest(String requesterUsername, String addresseeUsername) throws Exception;

    /**
     * Retrieves the friend list for a user with friendship status.
     * 
     * @param username The username to get friends for
     * @return List of friends with their status
     */
    List<FriendStatusDto> getFriendsList(String username);

    /**
     * Removes a user from the current user's friend list.
     * 
     * @param currentUsername The current user's username
     * @param friendUsername The friend's username to remove
     * @return The updated friendship record
     * @throws Exception if friendship doesn't exist or cannot be removed
     */
    Friendship unfriend(String currentUsername, String friendUsername) throws Exception;

    /**
     * Blocks a user, preventing any interaction.
     * 
     * @param blockerUsername The username of the user doing the blocking
     * @param blockedUsername The username of the user being blocked
     * @return The updated friendship record
     */
    Friendship blockUser(String blockerUsername, String blockedUsername);

    /**
     * Unblocks a previously blocked user.
     * Only the user who performed the blocking can unblock.
     * 
     * @param currentUserUsername The username of the user performing the unblock operation
     * @param blockedUsername The username of the user being unblocked
     * @return The updated friendship record
     * @throws Exception if relationship doesn't exist, status is incorrect, or no permission
     */
    Friendship unblockUser(String currentUserUsername, String blockedUsername) throws Exception;

    /**
     * Utility method to find friendship relationship between two users.
     * Can be used by other services (like MessageService) to validate relationships.
     * 
     * @param user1 The first user
     * @param user2 The second user
     * @return Optional containing the friendship if it exists, empty otherwise
     */
    Optional<Friendship> findFriendshipRelation(User user1, User user2);
}
