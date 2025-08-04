package com.eric.securechat.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * User entity representing a registered user in the secure chat system.
 * Implements Spring Security's UserDetails interface for authentication.
 * Contains user credentials, profile information, and cryptographic keys for end-to-end encryption.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Unique login handle for the user. Must be unique and should not be changed.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * User's display name. Can be changed and can be duplicated.
     */
    @Column(nullable = false)
    private String nickname;

    /**
     * Encrypted password for user authentication.
     */
    @Column(nullable = false)
    private String password;

    /**
     * X25519 public key for end-to-end encryption.
     */
    @Column(name = "x25519_public_key", columnDefinition = "TEXT")
    private String x25519PublicKey;

    /**
     * Ed25519 public key for digital signatures.
     */
    @Column(name = "ed25519_public_key", columnDefinition = "TEXT")
    private String ed25519PublicKey;

    /**
     * Timestamp when the user account was created.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the user account was last updated.
     */
    private Instant updatedAt;

    /**
     * Sets the creation timestamp before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    /**
     * Sets the update timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getX25519PublicKey() { return x25519PublicKey; }
    public void setX25519PublicKey(String x25519PublicKey) { this.x25519PublicKey = x25519PublicKey; }
    public String getEd25519PublicKey() { return ed25519PublicKey; }
    public void setEd25519PublicKey(String ed25519PublicKey) { this.ed25519PublicKey = ed25519PublicKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // UserDetails Methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
