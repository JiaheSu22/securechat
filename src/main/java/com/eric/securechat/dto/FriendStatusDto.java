package com.eric.securechat.dto;

import com.eric.securechat.model.FriendshipStatus;
import java.util.UUID;

/**
 * DTO for returning a user's info along with their friendship status.
 */
public record FriendStatusDto(
        UUID id,
        String username,
        String nickname,
        FriendshipStatus status,
        String ed25519PublicKey,
        String x25519PublicKey
) {}