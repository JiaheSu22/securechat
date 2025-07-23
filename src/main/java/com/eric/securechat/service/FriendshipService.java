package com.eric.securechat.service;

import com.eric.securechat.dto.FriendRequestViewDto;
import com.eric.securechat.dto.FriendStatusDto;
import com.eric.securechat.dto.UserDto;
import com.eric.securechat.model.Friendship;
import com.eric.securechat.model.User;

import java.util.List;
import java.util.Optional;

public interface FriendshipService {

    /**
     * 【新增】获取当前用户收到的、所有待处理的好友请求
     * @param currentUsername 当前登录用户的用户名
     * @return 一个包含好友请求信息的 DTO 列表
     */
    List<FriendRequestViewDto> getPendingRequests(String currentUsername);

    /**
     * 发送一个好友请求。
     * @param requesterUsername 发起请求的用户名
     * @param addresseeUsername 接收请求的用户名
     * @return 创建的 Friendship 记录
     * @throws Exception 如果用户不存在、已是好友、或请求已存在等
     */
    Friendship sendRequest(String requesterUsername, String addresseeUsername) throws Exception;

    /**
     * 接受一个好友请求。
     * @param requesterUsername 发起原始请求的用户名
     * @param addresseeUsername 接受请求的当前用户（即当初的接收者）
     * @return 更新后的 Friendship 记录
     * @throws Exception 如果请求不存在或状态不正确等
     */
    Friendship acceptRequest(String requesterUsername, String addresseeUsername) throws Exception;

    /**
     * 拒绝一个好友请求。
     * @param requesterUsername 发起原始请求的用户名
     * @param addresseeUsername 拒绝请求的当前用户（即当初的接收者）
     * @return 更新后的 Friendship 记录
     * @throws Exception 如果请求不存在或状态不正确等
     */
    Friendship declineRequest(String requesterUsername, String addresseeUsername) throws Exception;

    List<FriendStatusDto> getFriendsList(String username);

    Friendship unfriend(String currentUsername, String friendUsername) throws Exception;

    Friendship blockUser(String blockerUsername, String blockedUsername);

    /**
     * 解除对用户的拉黑。
     * 只有执行拉黑的用户才能解除拉黑。
     * @param currentUserUsername 执行解除拉黑操作的用户名 (即当初的拉黑者)
     * @param blockedUsername 被拉黑的用户名
     * @return 代表被删除关系的瞬时 Friendship 对象
     * @throws Exception 如果关系不存在、状态不正确或无权操作
     */
    Friendship unblockUser(String currentUserUsername, String blockedUsername) throws Exception;

    /**
     * [工具方法] 查找两个用户之间的好友关系，不关心方向。
     * 可供其他服务（如MessageService）调用来验证关系。
     * @param user1 第一个用户
     * @param user2 第二个用户
     * @return 包含 Friendship 的 Optional, 如果不存在任何关系则为空
     */
    Optional<Friendship> findFriendshipRelation(User user1, User user2);
}
