package com.eric.securechat.service;

import com.eric.securechat.dto.MessageResponse;
import com.eric.securechat.dto.SendMessageRequest;
import com.eric.securechat.exception.UserNotFoundException;
import com.eric.securechat.model.Message;
import com.eric.securechat.model.MessageType;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.MessageRepository;
import com.eric.securechat.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     *
     * Creates and saves a new message from a specified sender to a receiver.
     * This method is designed to be called by the controller, which provides the sender's identity.
     * The entire operation is transactional, ensuring data integrity.
     *
     * @param senderUsername The username of the user sending the message.
     * @param request The request DTO containing the receiver and message content.
     * @return The newly saved Message entity. (返回实体而非DTO，以供Controller后续处理)
     */
    @Transactional
    public Message sendMessage(String senderUsername, SendMessageRequest request) {

        logger.info("Attempting to send message from '{}' to '{}'. Type: {}",
                senderUsername, request.receiverUsername(), request.messageType());

        // 1. Find the sender and receiver User entities from the database.
        // The sender is identified by the username from the security context, passed in by the controller.
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Authenticated sender user not found: " + senderUsername));

        User receiver = userRepository.findByUsername(request.receiverUsername())
                .orElseThrow(() -> {
                    logger.warn("Message sending failed. Receiver user '{}' not found.", request.receiverUsername());
                    return new UserNotFoundException("Receiver user not found: " + request.receiverUsername());
                });

        // 2. Security check: prevent users from sending messages to themselves.
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same person.");
        }

        // 3. Create and populate a new Message entity.
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setEncryptedContent(request.encryptedContent());
        message.setMessageType(request.messageType());
        // 注意：我们不再手动设置时间戳 (message.setTimestamp(Instant.now()))
        // 而是依赖你在 Message 实体中定义的 @PrePersist 注解来自动完成，这更健壮。

        // ===== 新增逻辑：如果是文件消息，保存文件信息 =====
        if (request.messageType() == MessageType.FILE) {
            if (request.fileUrl() == null || request.originalFilename() == null) {
                // 做一个基本的数据校验
                throw new IllegalArgumentException("File message must contain fileUrl and originalFilename.");
            }
            message.setFileUrl(request.fileUrl());
            message.setOriginalFilename(request.originalFilename());
        }

        logger.info("Message from '{}' to '{}' saved successfully with ID '{}'.",
                sender.getUsername(), receiver.getUsername(), message.getId()); // <-- 数据库插入操作的日志
        // 4. Save the message to the database and return the persisted entity.
        return messageRepository.save(message);
    }

    /**
     *
     * Retrieves the full conversation history between two specified users.
     * The operation is marked as read-only for performance optimization.
     * This version is decoupled from the Security Context for better testability.
     *
     * @param currentUsername The username of the first participant (typically the authenticated user).
     * @param otherUsername The username of the second participant in the conversation.
     * @return A list of Message entities, sorted by timestamp. (返回实体，让Controller处理转换)
     */
    @Transactional(readOnly = true)
    public List<Message> getConversation(String currentUsername, String otherUsername) {
        // 1. Find the User entities for both participants.
        // The service now simply trusts the usernames provided by the controller.
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        User otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + otherUsername));

        // 2. Call the custom repository method to fetch the conversation.
        // 注意：我们直接返回 List<Message>，将DTO转换的责任留给Controller。
        // 这与我们对 sendMessage 的修改保持了一致性。
        return messageRepository.findConversation(currentUser.getId(), otherUser.getId());
    }


    /**
     * A private helper method to encapsulate the logic of converting a Message entity to a MessageResponse DTO.
     * @param message The Message entity to convert.
     * @return The resulting MessageResponse DTO.
     */
    private MessageResponse convertToResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getUsername(),
                message.getReceiver().getUsername(),
                message.getEncryptedContent(),
                message.getMessageType(),
                message.getTimestamp(),
                message.getFileUrl(),
                message.getOriginalFilename()
        );
    }
}
