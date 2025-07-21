package com.eric.securechat.dto;

/**
 * DTO to send user's public key info back to the client.
 * Converted to a Record.
 */
public record UserPublicKeyResponse(
        String username,
        String nickname,
        String publicKey
) {}
