package com.eric.securechat.dto;

import com.eric.securechat.model.MessageType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for message operations.
 * Contains message data for API responses and WebSocket notifications.
 */
public record MessageResponse(
        UUID id,
        String senderUsername,
        String receiverUsername,
        String encryptedContent,
        MessageType messageType,
        Instant timestamp,
        String fileUrl,
        String originalFilename,
        String nonce
) {}
