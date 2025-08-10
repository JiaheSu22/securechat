package com.eric.securechat.chat.web;

import com.eric.securechat.chat.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket Controller for handling real-time chat messages.
 * Processes incoming WebSocket messages and forwards them to appropriate recipients.
 * Uses Spring's STOMP messaging for secure message routing.
 */
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Processes incoming chat messages via WebSocket.
     * Securely identifies the sender using the WebSocket session principal.
     * Forwards the message to the specified recipient's private queue.
     * 
     * @param chatMessage The chat message payload containing message details
     * @param principal The authenticated user principal from the WebSocket session
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String senderUsername = principal.getName();
        chatMessage.setSenderUsername(senderUsername);

        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverUsername(),
                "/queue/messages",
                chatMessage
        );
    }
}
