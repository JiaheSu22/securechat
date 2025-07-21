package com.eric.securechat.dto;

import com.eric.securechat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for a client request to send a new message.
 * Now implemented as a Record for conciseness and immutability.
 */
public record SendMessageRequest(
        @NotBlank
        String receiverUsername,

        @NotBlank
        String encryptedContent,

        @NotNull
        MessageType messageType,

        String fileUrl,

        String originalFilename
) {
}
