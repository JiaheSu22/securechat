package com.eric.securechat.dto;

import jakarta.validation.constraints.NotBlank;

public class PublicKeyUploadRequest {

    @NotBlank(message = "Public key cannot be blank")
    private String publicKey;

    // Getters and Setters
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
