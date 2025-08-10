package com.eric.securechat.auth.dto;

/**
 * Request DTO for user login operations.
 * Contains user credentials for authentication.
 */
public record LoginRequest(String username, String password) {
}