package com.eric.securechat.message.repository;

import com.eric.securechat.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Message entity operations.
 * Provides data access methods for message management and conversation queries.
 */
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Finds all messages exchanged between two users, sorted by timestamp.
     *
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     * @return A list of messages forming the conversation
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);

    /**
     * Deletes all messages exchanged between two users.
     *
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE " +
            "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id)")
    void deleteConversation(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);

    /**
     * Finds messages containing specific text in their encrypted content.
     *
     * @param text The text to search for
     * @return List of messages containing the specified text
     */
    @Query("SELECT m FROM Message m WHERE m.encryptedContent LIKE %:text%")
    List<Message> findMessagesContainingText(@Param("text") String text);
}
