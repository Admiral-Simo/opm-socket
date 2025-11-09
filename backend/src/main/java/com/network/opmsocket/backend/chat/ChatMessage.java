package com.network.opmsocket.backend.chat;

/**
 * Represents a simple message sent from the client.
 * Jackson will deserialize the JSON {"content": "..."} into this object.
 */
public class ChatMessage {
    private String content;

    public ChatMessage() {
    }

    // Getter
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}