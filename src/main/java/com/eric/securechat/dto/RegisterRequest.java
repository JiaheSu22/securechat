// src/main/java/com/eric/securechat/dto/RegisterRequest.java
package com.eric.securechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration.
 * Converted to a Record.
 */
public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Nickname cannot be blank")
        String nickname
) {}
