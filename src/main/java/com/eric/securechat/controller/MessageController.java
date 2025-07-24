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

import com.eric.securechat.dto.SendMessageRequest; // 确保导入
import com.eric.securechat.model.MessageType; // 确保导入
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for handling message-related operations.
 * All endpoints under /api/messages require user authentication.
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    private final WebSocketService webSocketService;

    @Autowired
    public MessageController(MessageService messageService, @Autowired(required = false) WebSocketService webSocketService) {
        this.messageService = messageService;
        this.webSocketService = webSocketService;
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
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderUsername = authentication.getName();

        // 1. 调用 service 方法，它返回 Message 实体
        Message savedMessage = messageService.sendMessage(senderUsername, request);

        // 2. 将实体转换为 DTO
        MessageResponse response = new MessageResponse(
                savedMessage.getId(),
                savedMessage.getSender().getUsername(),
                savedMessage.getReceiver().getUsername(),
                savedMessage.getEncryptedContent(),
                savedMessage.getMessageType(),
                savedMessage.getTimestamp(),
                savedMessage.getFileUrl(),
                savedMessage.getOriginalFilename(),
                savedMessage.getNonce() // 新增
        );

        // 3. WebSocket 通知
        if (webSocketService != null) {
            webSocketService.notifyUser(response.receiverUsername(), response);
        }

        // 4. HTTP 响应
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
                        msg.getNonce() // 新增
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
