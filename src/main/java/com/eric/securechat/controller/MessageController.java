package com.eric.securechat.controller;

import com.eric.securechat.dto.MessageResponse;
import com.eric.securechat.dto.SendMessageRequest;
import com.eric.securechat.model.Message;
import com.eric.securechat.service.MessageService;
import com.eric.securechat.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for handling message-related operations.
 * All endpoints under /api/messages require user authentication.
 * Provides functionality for sending messages and retrieving conversation history.
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final WebSocketService webSocketService;

    /**
     * Constructor for MessageController.
     * 
     * @param messageService The service for handling message operations
     * @param webSocketService The service for WebSocket notifications (optional)
     */
    @Autowired
    public MessageController(MessageService messageService, @Autowired(required = false) WebSocketService webSocketService) {
        this.messageService = messageService;
        this.webSocketService = webSocketService;
    }

    /**
     * Endpoint to send a new message.
     * The sender is automatically determined from the authenticated user's security context.
     * Supports both text and file messages with encrypted content.
     * 
     * @param request The request containing receiver username, encrypted content, and message metadata
     * @return ResponseEntity containing the created message response with 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderUsername = authentication.getName();

        Message savedMessage = messageService.sendMessage(senderUsername, request);

        MessageResponse response = new MessageResponse(
                savedMessage.getId(),
                savedMessage.getSender().getUsername(),
                savedMessage.getReceiver().getUsername(),
                savedMessage.getEncryptedContent(),
                savedMessage.getMessageType(),
                savedMessage.getTimestamp(),
                savedMessage.getFileUrl(),
                savedMessage.getOriginalFilename(),
                savedMessage.getNonce()
        );

        if (webSocketService != null) {
            webSocketService.notifyUser(response.receiverUsername(), response);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve the conversation history with another user.
     * The current user is automatically determined from the security context.
     * Returns messages in chronological order.
     * 
     * @param otherUsername The username of the other user in the conversation
     * @return ResponseEntity containing a list of message responses with 200 OK status
     */
    @GetMapping("/{otherUsername}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable String otherUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<Message> messages = messageService.getConversation(currentUsername, otherUsername);
        List<MessageResponse> response = messages.stream()
                .map(msg -> new MessageResponse(
                        msg.getId(),
                        msg.getSender().getUsername(),
                        msg.getReceiver().getUsername(),
                        msg.getEncryptedContent(),
                        msg.getMessageType(),
                        msg.getTimestamp(),
                        msg.getFileUrl(),
                        msg.getOriginalFilename(),
                        msg.getNonce()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
