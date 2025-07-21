package com.eric.securechat.repository;

import com.eric.securechat.model.Message;
import com.eric.securechat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Finds all messages exchanged between two users, sorted by timestamp.
     *
     * @param user1Id The ID of the first user.
     * @param user2Id The ID of the second user.
     * @return A list of messages forming the conversation.
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);


    @Query("SELECT m FROM Message m WHERE m.encryptedContent LIKE %:text%")
    List<Message> findMessagesContainingText(@Param("text") String text);
}
