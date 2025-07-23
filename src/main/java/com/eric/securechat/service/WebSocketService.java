package com.eric.securechat.service;

import com.eric.securechat.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notifies a specific user about a new message by sending it over WebSocket.
     *
     * @param username The username of the recipient.
     * @param message  The message DTO to send.
     */
    public void notifyUser(String username, MessageResponse message) {
        // The destination is "/queue/private". Spring prepends "/user/{username}" automatically.
        // So the message is sent to a user-specific destination like "/user/bob/queue/private".
        // The client must be subscribed to "/user/queue/private" to receive it.
        String destination = "/queue/private";
        messagingTemplate.convertAndSendToUser(username, destination, message);

        logger.info("Sent message via WebSocket to user: '{}' at destination '{}'", username, destination);
    }
}
