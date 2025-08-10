package com.eric.securechat.friendship.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for Friendship entity.
 * Must implement Serializable and provide equals/hashCode methods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipId implements Serializable {

    private UUID requesterId;
    private UUID addresseeId;
}
