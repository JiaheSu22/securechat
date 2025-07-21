package com.eric.securechat.controller;

import com.eric.securechat.dto.MessageResponse;
import com.eric.securechat.dto.SendMessageRequest;
import com.eric.securechat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for handling message-related operations.
 * All endpoints under /api/messages require user authentication.
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Endpoint to send a new message.
     * Accessible via POST /api/messages
     * The request body should contain the receiver's username and the encrypted content.
     * The sender is automatically determined from the authenticated user's security context.
     *
     * @param request The request DTO.
     * @return A ResponseEntity containing the created message DTO with a 201 CREATED status.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        // 【修正】调用 Service 层的方法，它内部会自己获取 sender 信息。
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint to retrieve the conversation history with another user.
     * Accessible via GET /api/messages/{otherUsername}
     * The {otherUsername} is a path variable specifying the other participant.
     * The current user is automatically determined from the security context.
     *
     * @param otherUsername The username of the other user in the conversation.
     * @return A ResponseEntity containing a list of message DTOs with a 200 OK status.
     */
    @GetMapping("/{otherUsername}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable String otherUsername) {
        // 【修正】调用 Service 层的方法，它内部会自己获取 current user 信息。
        List<MessageResponse> conversation = messageService.getConversation(otherUsername);
        return ResponseEntity.ok(conversation);
    }
}
