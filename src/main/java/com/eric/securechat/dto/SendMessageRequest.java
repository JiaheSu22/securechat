package com.eric.securechat.dto;

/**
 * DTO for a client request to send a new message.
 */
public class SendMessageRequest {

    /**
     * The username of the message recipient.
     */
    private String receiverUsername;

    /**
     * The end-to-end encrypted content of the message.
     */
    private String encryptedContent;

    // --- Getters and Setters ---

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
}
