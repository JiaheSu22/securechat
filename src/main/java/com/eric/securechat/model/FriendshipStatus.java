package com.eric.securechat.model;

/**
 * 代表好友关系状态的枚举。
 * - PENDING: 请求已发送，等待对方响应。
 * - ACCEPTED: 请求已被接受，双方成为好友。
 * - DECLINED: 请求已被拒绝。
 * - BLOCKED: 一方已将另一方拉黑（为未来功能预留）。
 */
public enum FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    BLOCKED
}