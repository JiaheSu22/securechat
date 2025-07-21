package com.eric.securechat.dto;

import java.time.Instant;
import java.util.UUID;

public class MessageResponse {

    private UUID id;
    private String senderUsername;
    private String receiverUsername;
    private String encryptedContent;
    private Instant timestamp;

    public MessageResponse(UUID id, String senderUsername, String receiverUsername, String encryptedContent, Instant timestamp) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.encryptedContent = encryptedContent;
        this.timestamp = timestamp;
    }

    // --- Getters and Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }



    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
