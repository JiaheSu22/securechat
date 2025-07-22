package com.eric.securechat.service;

import com.eric.securechat.dto.UserDto;
import com.eric.securechat.model.Friendship;
import com.eric.securechat.model.FriendshipId;
import com.eric.securechat.model.FriendshipStatus;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.FriendshipRepository;
import com.eric.securechat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipServiceImpl(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * 发送好友请求。
     * 1. 检查是否对自己发送请求。
     * 2. 查找双方用户。
     * 3. 检查是否存在任何关系（PENDING, ACCEPTED, BLOCKED）。
     * 4. [要求 2] 如果被对方拉黑，则抛出异常。
     * 5. 创建新的 PENDING 关系。
     */
    @Override
    @Transactional
    public Friendship sendRequest(String requesterUsername, String addresseeUsername) throws Exception {
        log.info("Processing friend request from '{}' to '{}'", requesterUsername, addresseeUsername);

        if (requesterUsername.equals(addresseeUsername)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(addresseeUsername);


        Optional<Friendship> existingFriendshipOpt = findFriendshipRelation(requester, addressee);

        if (existingFriendshipOpt.isPresent()) {
            Friendship existingFriendship = existingFriendshipOpt.get();
            FriendshipStatus status = existingFriendship.getStatus();
            log.warn("Cannot send request from '{}' to '{}'. An existing relationship was found with status: {}",
                    requesterUsername, addresseeUsername, status);

            if (status == FriendshipStatus.BLOCKED) {
                // 检查是不是对方(addressee)拉黑了你(requester)
                if (existingFriendship.getActionUser() != null && existingFriendship.getActionUser().equals(addressee)) {
                    throw new IllegalStateException("You are blocked by this user and cannot send a friend request.");
                } else {
                    // 默认是你拉黑了对方，或无明确拉黑者记录
                    throw new IllegalStateException("You have blocked this user. Please unblock them to send a request.");
                }
            }
            if (status == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("You are already friends.");
            }
            throw new IllegalStateException("A friend request already exists between you two.");
        }

        // 创建新的好友关系记录，并明确记录操作者
        Friendship friendship = new Friendship(requester, addressee);
        friendship.setActionUser(requester);

        log.info("Friend request from '{}' to '{}' created successfully.", requesterUsername, addresseeUsername);
        return friendshipRepository.save(friendship);
    }

    /**
     * 接受好友请求。
     * 方法签名不变，但参数名更清晰：requesterUsername 是请求发起者，currentUserUsername 是当前操作者（即接收者）。
     */
    @Override
    @Transactional
    public Friendship acceptRequest(String requesterUsername, String currentUserUsername) throws Exception {
        log.info("User '{}' is attempting to accept a friend request from '{}'.", currentUserUsername, requesterUsername);

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(currentUserUsername); // 当前用户是接收者

        // 注意：接受请求时，请求方向是固定的 (requester -> addressee)
        FriendshipId friendshipId = new FriendshipId(requester.getId(), addressee.getId());
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalStateException("Friend request from '" + requesterUsername + "' not found."));

        // 验证逻辑
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("This friend request is not pending. Current status: " + friendship.getStatus());
        }
        // 安全检查：虽然我们的 Controller 可能已经保证了这一点，但在 Service 层再次验证是好习惯
        if (!friendship.getAddressee().equals(addressee)) {
            throw new SecurityException("You are not authorized to accept this request.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setActionUser(addressee); // 记录是接收者接受了请求

        log.info("User '{}' successfully accepted the friend request from '{}'.", currentUserUsername, requesterUsername);
        return friendshipRepository.save(friendship);
    }

    /**
     * 拒绝好友请求。
     * 方法签名不变，逻辑保持为删除记录并返回一个瞬时对象，以兼容 Controller。
     */
    @Override
    @Transactional
    public Friendship declineRequest(String requesterUsername, String currentUserUsername) throws Exception {
        log.info("User '{}' is attempting to decline a friend request from '{}'.", currentUserUsername, requesterUsername);

        User requester = findUserByUsername(requesterUsername);
        User addressee = findUserByUsername(currentUserUsername);

        FriendshipId friendshipId = new FriendshipId(requester.getId(), addressee.getId());
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalStateException("Friend request from '" + requesterUsername + "' not found."));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("This request is not pending.");
        }
        if (!friendship.getAddressee().equals(addressee)) {
            throw new SecurityException("You are not authorized to decline this request.");
        }

        friendshipRepository.delete(friendship);
        log.info("Friend request from '{}' was declined and removed by '{}'.", requesterUsername, currentUserUsername);

        // 为了兼容旧的接口签名，返回一个已从数据库删除的临时对象
        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendship;
    }

    /**
     * 解除好友关系。
     * 使用辅助方法简化了查找逻辑。
     */
    @Override
    @Transactional
    public Friendship unfriend(String currentUsername, String friendUsername) throws Exception {
        log.info("User '{}' is attempting to unfriend '{}'.", currentUsername, friendUsername);

        User currentUser = findUserByUsername(currentUsername);
        User friendToUnfriend = findUserByUsername(friendUsername);

        Friendship friendship = findFriendshipRelation(currentUser, friendToUnfriend)
                .orElseThrow(() -> new IllegalStateException("Friendship record not found between " + currentUsername + " and " + friendUsername));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            log.warn("Unfriend attempt failed. Users '{}' and '{}' are not friends. Current status: {}", currentUsername, friendUsername, friendship.getStatus());
            throw new IllegalStateException("You can only unfriend someone who is currently your friend.");
        }

        friendshipRepository.delete(friendship);
        log.info("User '{}' successfully unfriended '{}'.", currentUsername, friendUsername);

        // 同样，为兼容接口，返回被删除的实体
        return friendship;
    }

    /**
     * 拉黑用户。
     * @param blockerUsername 执行拉黑操作的用户名
     * @param blockedUsername 被拉黑的用户名
     * @return 更新或创建后的 Friendship 记录
     */
    @Override
    @Transactional
    public Friendship blockUser(String blockerUsername, String blockedUsername) {
        log.info("User '{}' is attempting to block user '{}'.", blockerUsername, blockedUsername);

        User blocker = findUserByUsername(blockerUsername);
        User blocked = findUserByUsername(blockedUsername);

        Optional<Friendship> friendshipOpt = findFriendshipRelation(blocker, blocked);
        Friendship friendship;

        if (friendshipOpt.isPresent()) {
            // 关系已存在，更新状态
            log.info("Found existing relationship between '{}' and '{}'. Updating status to BLOCKED.", blockerUsername, blockedUsername);
            friendship = friendshipOpt.get();
        } else {
            // 关系不存在，创建一个新的
            log.info("No existing relationship found. Creating new BLOCKED relationship for '{}' and '{}'.", blockerUsername, blockedUsername);
            // 为保证复合主键的顺序一致性，我们手动决定谁是 requester，谁是 addressee
            // 这可以防止 (A,B) 和 (B,A) 同时存在，虽然 DB unique constraint 也能防止，但这是个好习惯
            User requester, addressee;
            if (blocker.getId().toString().compareTo(blocked.getId().toString()) < 0) {
                requester = blocker;
                addressee = blocked;
            } else {
                requester = blocked;
                addressee = blocker;
            }
            friendship = new Friendship(requester, addressee);
        }

        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setActionUser(blocker); // 明确记录是 blocker 执行了拉黑操作

        return friendshipRepository.save(friendship);
    }

    /**
     * 获取好友列表。
     * 使用了在 Repository 中新增的 `findAllByUserAndStatus` 方法，将两次数据库查询合并为一次。
     */
    @Override
    public List<UserDto> getFriendsList(String username) {
        log.debug("Fetching friend list for user '{}'", username);
        User currentUser = findUserByUsername(username);

        // 使用新的、更高效的查询
        List<Friendship> acceptedFriendships = friendshipRepository.findAllByUserAndStatus(currentUser, FriendshipStatus.ACCEPTED);

        return acceptedFriendships.stream()
                // 对于每一条好友关系，如果我是请求者，则好友是接收者，反之亦然
                .map(friendship ->
                        friendship.getRequester().equals(currentUser) ? friendship.getAddressee() : friendship.getRequester()
                )
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }

    // --- 私有辅助方法 ---

    /**
     * [辅助方法] 根据用户名查找用户，对 "用户未找到" 的情况进行统一处理。
     * @param username 用户名
     * @return User 实体
     * @throws UsernameNotFoundException 如果用户不存在
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    @Transactional
    public Friendship unblockUser(String currentUserUsername, String blockedUsername) throws Exception {
        log.info("User '{}' is attempting to unblock user '{}'.", currentUserUsername, blockedUsername);

        User currentUser = findUserByUsername(currentUserUsername);
        User userToUnblock = findUserByUsername(blockedUsername);

        Friendship friendship = findFriendshipRelation(currentUser, userToUnblock)
                .orElseThrow(() -> new IllegalStateException("No relationship record found with user: " + blockedUsername));

        // 验证状态是否为 BLOCKED
        if (friendship.getStatus() != FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("This user is not blocked.");
        }

        // 验证操作者是否是当初的拉黑者
        if (friendship.getActionUser() == null || !friendship.getActionUser().equals(currentUser)) {
            log.warn("Security check failed: User '{}' tried to unblock '{}', but the original blocker was '{}'.",
                    currentUserUsername, blockedUsername,
                    friendship.getActionUser() != null ? friendship.getActionUser().getUsername() : "unknown");
            throw new SecurityException("Only the user who initiated the block can unblock.");
        }

        // 解除拉黑 = 删除关系记录，恢复陌生人状态
        friendshipRepository.delete(friendship);
        log.info("User '{}' successfully unblocked user '{}'. The relationship record has been removed.", currentUserUsername, blockedUsername);

        // 兼容接口，返回被删除的实体
        return friendship;
    }

    /**
     * [辅助方法] 查找两个用户之间的好友关系，不关心方向。
     * 核心重构方法，利用 Repository 中已有的 @Query 方法。
     * @param user1 第一个用户
     * @param user2 第二个用户
     * @return 包含 Friendship 的 Optional
     */
    private Optional<Friendship> findFriendshipRelation(User user1, User user2) {
        // 直接调用 Repository 中已经定义好的双向查询方法
        return friendshipRepository.findFriendshipBetweenUsers(user1, user2);
    }
}
