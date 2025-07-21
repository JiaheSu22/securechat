package com.eric.securechat.service;

import com.eric.securechat.dto.MessageResponse;
import com.eric.securechat.dto.SendMessageRequest;
import com.eric.securechat.model.Message;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.MessageRepository;
import com.eric.securechat.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and saves a new message sent from the authenticated user to a specified receiver.
     * The entire operation is transactional, ensuring data integrity.
     *
     * @param request The request DTO containing the receiver and message content.
     * @return A DTO representation of the newly saved message.
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        // 1. Get the username of the currently authenticated user from the security context.
        String senderUsername = getCurrentUsername();

        // 2. Find the sender and receiver User entities from the database.
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database. This should not happen."));

        User receiver = userRepository.findByUsername(request.getReceiverUsername())
                .orElseThrow(() -> new IllegalArgumentException("Receiver user not found: " + request.getReceiverUsername()));

        // 3. Security check: prevent users from sending messages to themselves.
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same person.");
        }

        // 4. Create and populate a new Message entity.
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setEncryptedContent(request.getEncryptedContent());
        // The timestamp will be automatically set by the @PrePersist annotation in the Message entity.

        // 5. Save the message to the database.
        Message savedMessage = messageRepository.save(message);

        // 6. Convert the saved entity to a DTO and return it.
        return convertToResponse(savedMessage);
    }

    /**
     * Retrieves the full conversation history between the authenticated user and another specified user.
     * The operation is marked as read-only for performance optimization.
     *
     * @param otherUsername The username of the other participant in the conversation.
     * @return A list of MessageResponse DTOs, sorted by timestamp.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(String otherUsername) {
        // 1. Get the username of the currently authenticated user.
        String currentUsername = getCurrentUsername();

        // 2. Find the User entities for both participants in the conversation.
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        User otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Other user not found: " + otherUsername));

        // 3. Call the custom repository method to fetch the conversation.
        List<Message> messages = messageRepository.findConversation(currentUser, otherUser);

        // 4. Convert the list of Message entities to a list of MessageResponse DTOs.
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * A helper method to get the username of the currently authenticated user from Spring Security's context.
     * @return The username of the logged-in user.
     */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            // This case might happen in some testing scenarios or if the principal is a simple string.
            return principal.toString();
        }
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
                message.getTimestamp()
        );
    }
}
