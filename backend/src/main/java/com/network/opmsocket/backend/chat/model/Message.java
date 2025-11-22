package com.network.opmsocket.backend.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderName;

    @Column(columnDefinition = "TEXT") // Use TEXT for longer messages
    private String content;

    private Instant timestamp;

    // This method is called automatically before the entity is saved
    @PrePersist
    public void onPrePersist() {
        timestamp = Instant.now();
    }
}