package com.eric.securechat.dto;

import com.eric.securechat.model.MessageType;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for sending a message back to the client.
 * Now implemented as a Record.
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
