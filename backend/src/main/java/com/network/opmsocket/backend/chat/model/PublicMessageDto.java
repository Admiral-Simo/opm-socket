package com.network.opmsocket.backend.chat.model;
import lombok.Data;

import java.time.Instant;

/**
 * Represents a public message broadcast from the server.
 * Jackson will serialize this object into JSON {"senderName": "...", "content":
 * "..."}
 */
@Data
public class PublicMessageDto {
    private String senderName;
    private String content;
    private Instant timestamp;

    public PublicMessageDto(String senderName, String content, Instant timestamp) {
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }
}