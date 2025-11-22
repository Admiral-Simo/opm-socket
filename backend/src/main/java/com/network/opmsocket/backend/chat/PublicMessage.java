package com.network.opmsocket.backend.chat;
import lombok.Data;

import java.time.Instant;

/**
 * Represents a public message broadcast from the server.
 * Jackson will serialize this object into JSON {"senderName": "...", "content":
 * "..."}
 */
@Data
public class PublicMessage {
    private String senderName;
    private String content;
    private Instant timestamp;

    public PublicMessage(String senderName, String content, Instant timestamp) {
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }
}