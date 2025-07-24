package com.eric.securechat.service;

import com.eric.securechat.dto.MessageResponse;       // <<< 修改：使用你已有的Record
import com.eric.securechat.dto.SendMessageRequest;
import com.eric.securechat.exception.UserNotFoundException;
import com.eric.securechat.model.Friendship;
import com.eric.securechat.model.FriendshipStatus;
import com.eric.securechat.model.Message;
import com.eric.securechat.model.MessageType;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.MessageRepository;
import com.eric.securechat.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    // --- 依赖声明区 ---
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;


    // <<< 修改：构造函数，接收新增的依赖
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, FriendshipService friendshipService, SimpMessagingTemplate messagingTemplate, ModelMapper modelMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.messagingTemplate = messagingTemplate;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public Message sendMessage(String senderUsername, SendMessageRequest request) { // <<< 返回类型保持 Message 不变
        logger.info("Attempting to send message from '{}' to '{}'. Type: {}",
                senderUsername, request.receiverUsername(), request.messageType());

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Authenticated sender user not found: " + senderUsername));

        User receiver = userRepository.findByUsername(request.receiverUsername())
                .orElseThrow(() -> new UserNotFoundException("Receiver user not found: " + request.receiverUsername()));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same person.");
        }

        // ===== 好友关系验证，现在可以成功调用了 =====
        checkFriendshipStatus(sender, receiver);

        // 后续逻辑不变
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setEncryptedContent(request.encryptedContent());
        message.setMessageType(request.messageType());
        message.setNonce(request.nonce()); // 新增

        if (request.messageType() == MessageType.FILE) {
            if (request.fileUrl() == null || request.originalFilename() == null) {
                throw new IllegalArgumentException("File message must contain fileUrl and originalFilename.");
            }
            message.setFileUrl(request.fileUrl());
            message.setOriginalFilename(request.originalFilename());
        }

        // <<< 这里是核心修改区 START >>>
        // 1. 先保存消息到数据库
        Message savedMessage = messageRepository.save(message);
        logger.info("Message from '{}' to '{}' saved successfully.", sender.getUsername(), receiver.getUsername());

        // 2. 将保存后的 Message 实体转换为 MessageResponse record，用于 WebSocket 推送
        // ModelMapper 会自动处理从 message.sender.username 到 record.senderUsername 的映射
        MessageResponse messageToSend = new MessageResponse(
                savedMessage.getId(), //id的类型不一致，手动转换
                savedMessage.getSender().getUsername(),
                savedMessage.getReceiver().getUsername(),
                savedMessage.getEncryptedContent(),
                savedMessage.getMessageType(),
                savedMessage.getTimestamp(),
                savedMessage.getFileUrl(),
                savedMessage.getOriginalFilename(),
                savedMessage.getNonce() // 新增
        );

        // 3. 通过 WebSocket 将 record 推送给接收者
        String destination = "/queue/messages";
        logger.info("Pushing real-time message to user '{}' at destination '{}'", receiver.getUsername(), destination);
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                destination,
                messageToSend
        );
        logger.info("Message successfully pushed via WebSocket to {}", receiver.getUsername());

        // 4. 保持原有方法签名，返回原始的 Message 对象给HTTP调用方
        return savedMessage;
        // <<< 核心修改区 END >>>
    }

    @Transactional(readOnly = true)
    public List<Message> getConversation(String currentUsername, String otherUsername) { // 方法完全保持不变
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        User otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + otherUsername));

        // ===== 好友关系验证，现在可以成功调用了 =====
        checkFriendshipStatus(currentUser, otherUser);

        return messageRepository.findConversation(currentUser.getId(), otherUser.getId());
    }

    /**
     * 【关键修复】第3步：在这个私有方法中，使用我们刚刚声明并初始化的 this.friendshipService 字段
     * 因为 friendshipService 现在是类的成员，所以可以直接调用。
     * 检查两个用户之间的好友关系。如果关系不是 ACCEPTED，则抛出异常。
     *
     * @param userOne 第一个用户
     * @param userTwo 第二个用户
     * @throws IllegalStateException 如果关系不是好友或被拉黑
     */
    private void checkFriendshipStatus(User userOne, User userTwo) { // 方法完全保持不变
        // 这里调用的 friendshipService 就是我们在构造函数里赋值的那个对象
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
