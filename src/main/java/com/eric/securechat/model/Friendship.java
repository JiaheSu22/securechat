package com.eric.securechat.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})})
public class Friendship {

    // 使用 @EmbeddedId 注解来声明一个嵌入式的复合主键
    @EmbeddedId
    private FriendshipId id;

    // --- 关系映射 ---

    // @ManyToOne 定义了多对一关系
    // @MapsId("requesterId") 告诉 JPA，这个 User 实体映射到复合主键 id 中的 "requesterId" 字段
    // insertable=false, updatable=false 表示这个关系字段本身不直接写入数据库的列，它的值由 id.requesterId 控制
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requesterId")
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addresseeId")
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user_id")
    private User actionUser;

    // 提供一个方便的构造函数
    public Friendship(User requester, User addressee) {
        // 创建复合主键实例
        this.id = new FriendshipId(requester.getId(), addressee.getId());
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }
}
