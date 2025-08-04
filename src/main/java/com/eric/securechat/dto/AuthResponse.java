package com.eric.securechat.dto;

/**
 * Response DTO for authentication operations.
 * Contains the JWT token for successful authentication.
 */
public record AuthResponse(String token) {
}
