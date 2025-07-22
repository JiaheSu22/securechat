package com.eric.securechat.dto;

import lombok.Data;

@Data
public class FriendRequestDto {
    // 为了通用性，我们用一个简单的 username 字段
    // 在发送请求时，它代表 "addresseeUsername"
    // 在接受/拒绝请求时，它代表 "requesterUsername"
    private String username;
}

