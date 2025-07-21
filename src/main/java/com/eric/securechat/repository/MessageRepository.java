package com.eric.securechat.repository;

import com.eric.securechat.model.Message;
import com.eric.securechat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Finds all messages exchanged between two users, sorted by timestamp.
     * This query fetches the complete conversation history.
     *
     * @param user1 The first user in the conversation.
     * @param user2 The second user in the conversation.
     * @return A list of messages forming the conversation.
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);
}
