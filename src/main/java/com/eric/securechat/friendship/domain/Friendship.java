package com.eric.securechat.friendship.domain;

import com.eric.securechat.user.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing friendship relationships between users.
 * Uses composite primary key to ensure unique relationships.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})})
public class Friendship {

    /**
     * Composite primary key for the friendship relationship.
     */
    @EmbeddedId
    private FriendshipId id;

    /**
     * The user who sent the friend request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requesterId")
    @JoinColumn(name = "requester_id")
    private User requester;

    /**
     * The user who received the friend request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addresseeId")
    @JoinColumn(name = "addressee_id")
    private User addressee;

    /**
     * Current status of the friendship relationship.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    /**
     * Timestamp when the friendship was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the friendship was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User who performed the last action on this friendship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user_id")
    private User actionUser;

    /**
     * Constructor for creating a new friendship relationship.
     * 
     * @param requester The user sending the friend request
     * @param addressee The user receiving the friend request
     */
    public Friendship(User requester, User addressee) {
        this.id = new FriendshipId(requester.getId(), addressee.getId());
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }
}
