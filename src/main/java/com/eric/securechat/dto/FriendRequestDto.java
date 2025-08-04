package com.eric.securechat.dto;

import lombok.Data;

/**
 * DTO for friend request operations.
 * Contains username for various friendship operations (send, accept, decline, block).
 */
@Data
public class FriendRequestDto {
    private String username;
}

