package com.eric.securechat.controller;

import com.eric.securechat.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Used to send messages

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // We can use the 'Principal' object from the WebSocket session to securely identify the sender.
        // This prevents a client from spoofing the 'from' field.
        String senderUsername = principal.getName();
        chatMessage.setFrom(senderUsername);

        // The destination for a specific user is typically "/user/{username}/queue/messages"
        messagingTemplate.convertAndSendToUser(
                chatMessage.getTo(),          // The recipient's username
                "/queue/messages",            // The specific queue on that user's channel
                chatMessage                   // The payload
        );
    }
}
