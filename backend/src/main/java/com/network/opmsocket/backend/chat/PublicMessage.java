package com.network.opmsocket.backend.chat;
import java.time.Instant;

/**
 * Represents a public message broadcast from the server.
 * Jackson will serialize this object into JSON {"senderName": "...", "content":
 * "..."}
 */
public class PublicMessage {
    private String senderName;
    private String content;
    private Instant timestamp;

    public PublicMessage(String senderName, String content, Instant timestamp) {
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}