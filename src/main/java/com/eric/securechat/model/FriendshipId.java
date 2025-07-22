package com.eric.securechat.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

/**
 * 这是 Friendship 实体的复合主键类。
 * 一个复合主键类必须满足以下条件：
 * 1. 必须有一个无参构造函数 (public)。
 * 2. 必须实现 java.io.Serializable 接口。
 * 3. 必须重写 equals() 和 hashCode() 方法。
 * 4. 必须被 @Embeddable 注解标记。
 */
@Data // Lombok 注解: 自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok 注解: 自动生成无参构造函数
@AllArgsConstructor // Lombok 注解: 自动生成全参构造函数
@EqualsAndHashCode // Lombok 注解: 确保 equals 和 hashCode 被正确实现
@Embeddable // JPA 注解: 表示这个类可以被嵌入到其他实体中
public class FriendshipId implements Serializable {

    // 注意：这里的字段名必须和 Friendship 实体中 @MapsId 注解指定的字段名完全一致！
    // 比如 Friendship 里的 requester 字段是 @MapsId("requesterId")，这里就是 requesterId。
    // 我们这里直接用 requester 和 addressee，更简洁。

    private UUID requesterId;
    private UUID addresseeId;

}
