package com.eric.securechat.message.dto;

import com.eric.securechat.message.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for sending a new message.
 * Contains message content and metadata with validation constraints.
 */
public record SendMessageRequest(
        @NotBlank
        String receiverUsername,

        @NotBlank
        String encryptedContent,

        @NotNull
        MessageType messageType,

        String fileUrl,

        String originalFilename,

        String nonce
) {
}
