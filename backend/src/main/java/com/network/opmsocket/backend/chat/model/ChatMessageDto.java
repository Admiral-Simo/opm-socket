package com.network.opmsocket.backend.chat.model;

import lombok.Data;

/**
 * Represents a simple message sent from the client.
 * Jackson will deserialize the JSON {"content": "..."} into this object.
 */
@Data
public class ChatMessageDto {
    private String content;
}