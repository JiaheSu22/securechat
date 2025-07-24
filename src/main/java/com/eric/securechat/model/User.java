package com.eric.securechat.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    // This is the login handle. It must be unique and should not be changed.
    @Column(nullable = false, unique = true)
    private String username;

    // This is the user's display name. It can be changed and can be duplicated.
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(name = "x25519_public_key", columnDefinition = "TEXT")
    private String x25519PublicKey;

    @Column(name = "ed25519_public_key", columnDefinition = "TEXT")
    private String ed25519PublicKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ... @PrePersist and @PreUpdate methods remain the same ...
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // --- Getters and Setters ---

    // Add Getter and Setter for nickname
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // ... all other getters and setters remain the same ...
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


    // --- UserDetails Methods remain the same ---
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
