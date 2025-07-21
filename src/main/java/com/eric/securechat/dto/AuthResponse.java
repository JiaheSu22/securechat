package com.eric.securechat.dto;

// 我们也统一使用 record 来响应，这更一致
public record AuthResponse(String token) {
}
