package com.network.opmsocket.backend.chat;

/**
 * Represents a public message broadcast from the server.
 * Jackson will serialize this object into JSON {"senderName": "...", "content":
 * "..."}
 */
public class PublicMessage {
    private String senderName;
    private String content;

    // No-arg constructor (required for Jackson)
    public PublicMessage() {
    }

    // All-arg constructor
    public PublicMessage(String senderName, String content) {
        this.senderName = senderName;
        this.content = content;
    }

    // Getters and setters
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}