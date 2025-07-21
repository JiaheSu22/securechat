package com.eric.securechat.model;

public class ChatMessage {
    private String from; // Sender's username
    private String to;   // Recipient's username
    private String content; // The encrypted payload (as a JSON string or Base64)

    // Getters and setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
