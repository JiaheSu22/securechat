package com.eric.securechat.repository;

import com.eric.securechat.model.Friendship;
import com.eric.securechat.model.FriendshipId;
import com.eric.securechat.model.FriendshipStatus;
import com.eric.securechat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Friendship entity operations.
 * Provides data access methods for friendship relationship management.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    /**
     * Finds a friendship request by requester and addressee.
     * This query is directional (one-way).
     *
     * @param requester The user who sent the request
     * @param addressee The user who received the request
     * @return Optional containing the friendship if found, empty otherwise
     */
    Optional<Friendship> findByRequesterAndAddressee(User requester, User addressee);

    /**
     * Finds friendship relationship between two users.
     * Checks both directions since requester and addressee can be swapped.
     *
     * @param user1 The first user
     * @param user2 The second user
     * @return Optional containing the friendship if found, empty otherwise
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Finds friendships where the user is the requester with specified status.
     * 
     * @param requester The user who sent the requests
     * @param status The friendship status to filter by
     * @return List of friendships matching the criteria
     */
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);
    
    /**
     * Finds friendships where the user is the addressee with specified status.
     * 
     * @param addressee The user who received the requests
     * @param status The friendship status to filter by
     * @return List of friendships matching the criteria
     */
    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    /**
     * Finds all friendships for a user with specified status.
     * Checks both requester and addressee roles for the user.
     * 
     * @param user The user to find friendships for
     * @param status The friendship status to filter by
     * @return List of friendships matching the criteria
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = :status")
    List<Friendship> findAllByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);
}
