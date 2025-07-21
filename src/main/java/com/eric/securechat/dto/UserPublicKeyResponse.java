package com.eric.securechat.dto;

public class UserPublicKeyResponse {

    private String username;
    private String nickname; // 包含 nickname!
    private String publicKey;

    public UserPublicKeyResponse(String username, String nickname, String publicKey) {
        this.username = username;
        this.nickname = nickname;
        this.publicKey = publicKey;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
