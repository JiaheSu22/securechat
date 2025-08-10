package com.eric.securechat.message.application;

import com.eric.securechat.message.dto.MessageResponse;
import com.eric.securechat.message.dto.SendMessageRequest;
import com.eric.securechat.user.exception.UserNotFoundException;
import com.eric.securechat.friendship.application.FriendshipService;
import com.eric.securechat.friendship.domain.Friendship;
import com.eric.securechat.friendship.domain.FriendshipStatus;
import com.eric.securechat.message.domain.Message;
import com.eric.securechat.message.domain.MessageType;
import com.eric.securechat.user.domain.User;
import com.eric.securechat.message.repository.MessageRepository;
import com.eric.securechat.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling message operations including sending messages and retrieving conversations.
 * Provides secure message handling with friendship validation and real-time WebSocket notifications.
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;

    /**
     * Constructor for MessageService.
     * 
     * @param messageRepository Repository for message data operations
     * @param userRepository Repository for user data operations
     * @param friendshipService Service for friendship validation
     * @param messagingTemplate Template for WebSocket messaging
     * @param modelMapper Mapper for object transformations
     */
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, FriendshipService friendshipService, SimpMessagingTemplate messagingTemplate, ModelMapper modelMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.messagingTemplate = messagingTemplate;
        this.modelMapper = modelMapper;
    }

    /**
     * Sends a message from one user to another with friendship validation.
     * Validates that both users exist and have an accepted friendship status.
     * Saves the message to database and sends real-time notification via WebSocket.
     * 
     * @param senderUsername The username of the message sender
     * @param request The message request containing receiver and content details
     * @return The saved message entity
     * @throws UserNotFoundException if sender or receiver is not found
     * @throws IllegalArgumentException if sender and receiver are the same
     * @throws IllegalStateException if friendship validation fails
     */
    @Transactional
    public Message sendMessage(String senderUsername, SendMessageRequest request) {
        logger.info("Attempting to send message from '{}' to '{}'. Type: {}",
                senderUsername, request.receiverUsername(), request.messageType());

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Authenticated sender user not found: " + senderUsername));

        User receiver = userRepository.findByUsername(request.receiverUsername())
                .orElseThrow(() -> new UserNotFoundException("Receiver user not found: " + request.receiverUsername()));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same person.");
        }

        checkFriendshipStatus(sender, receiver);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setEncryptedContent(request.encryptedContent());
        message.setMessageType(request.messageType());
        message.setNonce(request.nonce());

        if (request.messageType() == MessageType.FILE) {
            if (request.fileUrl() == null || request.originalFilename() == null) {
                throw new IllegalArgumentException("File message must contain fileUrl and originalFilename.");
            }
            message.setFileUrl(request.fileUrl());
            message.setOriginalFilename(request.originalFilename());
        }

        Message savedMessage = messageRepository.save(message);
        logger.info("Message from '{}' to '{}' saved successfully.", sender.getUsername(), receiver.getUsername());

        MessageResponse messageToSend = new MessageResponse(
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

        String destination = "/queue/messages";
        logger.info("Pushing real-time message to user '{}' at destination '{}'", receiver.getUsername(), destination);
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                destination,
                messageToSend
        );
        logger.info("Message successfully pushed via WebSocket to {}", receiver.getUsername());

        return savedMessage;
    }

    /**
     * Retrieves conversation history between two users.
     * Validates that both users exist and have an accepted friendship status.
     * 
     * @param currentUsername The username of the current user
     * @param otherUsername The username of the other user in the conversation
     * @return List of messages in the conversation
     * @throws UserNotFoundException if either user is not found
     * @throws IllegalStateException if friendship validation fails
     */
    @Transactional(readOnly = true)
    public List<Message> getConversation(String currentUsername, String otherUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        User otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + otherUsername));

        checkFriendshipStatus(currentUser, otherUser);

        return messageRepository.findConversation(currentUser.getId(), otherUser.getId());
    }

    /**
     * Validates the friendship status between two users.
     * Ensures that users have an accepted friendship status and are not blocked.
     * 
     * @param userOne The first user
     * @param userTwo The second user
     * @throws IllegalStateException if users are not friends or if relationship is blocked
     */
    private void checkFriendshipStatus(User userOne, User userTwo) {
        Optional<Friendship> friendshipOpt = friendshipService.findFriendshipRelation(userOne, userTwo);

        if (friendshipOpt.isEmpty()) {
            logger.warn("Action denied between '{}' and '{}'. Reason: They are not friends.", userOne.getUsername(), userTwo.getUsername());
            throw new IllegalStateException("You are not friends with this user.");
        }

        Friendship friendship = friendshipOpt.get();
        if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
            String blockerUsername = friendship.getActionUser().getUsername();
            logger.warn("Action denied between '{}' and '{}'. Reason: Relationship is BLOCKED by '{}'.", userOne.getUsername(), userTwo.getUsername(), blockerUsername);

            if (userOne.getUsername().equals(blockerUsername)) {
                throw new IllegalStateException("You have blocked this user. Unblock them to interact.");
            } else {
                throw new IllegalStateException("You cannot interact with this user as you have been blocked.");
            }
        }

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            logger.warn("Action denied between '{}' and '{}'. Reason: Friendship status is {}.", userOne.getUsername(), userTwo.getUsername(), friendship.getStatus());
            throw new IllegalStateException("Your friendship is not active. Status: " + friendship.getStatus());
        }

        logger.debug("Friendship check passed between '{}' and '{}'. Status: ACCEPTED.", userOne.getUsername(), userTwo.getUsername());
    }
}
