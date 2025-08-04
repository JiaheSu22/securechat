package com.eric.securechat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for displaying friend request information.
 * Contains requester details and request timestamp.
 */
public record FriendRequestViewDto(
        UUID requesterId,
        String username,
        String nickname,
        LocalDateTime sentAt
) {
}