package com.eric.securechat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for a client request to upload their public key.
 * Converted to a Record for simplicity and immutability.
 */
public record PublicKeyUploadRequest(
        @NotBlank(message = "Public key cannot be blank")
        String publicKey
) {}
