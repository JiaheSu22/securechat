package com.eric.securechat.model;

import java.time.Instant;

/**
 * Represents a message DTO (Data Transfer Object) for WebSocket communication.
 * This class needs to accommodate both text and file messages.
 */
public class ChatMessage {
    // Renamed fields to match frontend payload for consistency
    private String senderUsername; // Replaces 'from'
    private String receiverUsername; // Replaces 'to'
    private String encryptedContent; // Replaces 'content'

    // New fields to support file messages
    private MessageType messageType; // Use an enum for type safety
    private String fileUrl;
    private String originalFilename;
    private String nonce;
    private Instant timestamp;

    // Enum for message types
    public enum MessageType {
        TEXT, FILE
    }

    // Getters and Setters

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

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
