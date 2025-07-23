package com.eric.securechat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用于展示好友请求信息的DTO
 * @param requesterId  请求者的UUID
 * @param username     请求者的用户名
 * @param nickname     请求者的昵称
 * @param sentAt       请求发送时间
 */
public record FriendRequestViewDto(
        // 注意：Friendship实体使用复合主键，直接暴露可能不便。
        // 这里我们主要需要请求者的信息，所以返回请求者的ID用于后续操作。
        UUID requesterId,
        String username,
        String nickname,
        LocalDateTime sentAt
) {
}