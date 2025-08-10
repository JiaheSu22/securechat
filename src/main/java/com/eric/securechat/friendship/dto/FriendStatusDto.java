package com.eric.securechat.friendship.dto;

import com.eric.securechat.friendship.domain.FriendshipStatus;
import java.util.UUID;

/**
 * DTO for user information with friendship status.
 * Contains user profile and cryptographic keys for secure communication.
 */
public record FriendStatusDto(
        UUID id,
        String username,
        String nickname,
        FriendshipStatus status,
        String ed25519PublicKey,
        String x25519PublicKey
) {}