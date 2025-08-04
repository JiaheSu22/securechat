package com.eric.securechat.service;

import com.eric.securechat.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for WebSocket messaging operations.
 * Handles real-time message delivery to specific users.
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor for WebSocketService.
     * 
     * @param messagingTemplate Template for WebSocket messaging operations
     */
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notifies a specific user about a new message by sending it over WebSocket.
     * Sends message to user-specific destination for real-time delivery.
     *
     * @param username The username of the recipient
     * @param message The message DTO to send
     */
    public void notifyUser(String username, MessageResponse message) {
        String destination = "/queue/private";
        messagingTemplate.convertAndSendToUser(username, destination, message);

        logger.info("Sent message via WebSocket to user: '{}' at destination '{}'", username, destination);
    }
}
