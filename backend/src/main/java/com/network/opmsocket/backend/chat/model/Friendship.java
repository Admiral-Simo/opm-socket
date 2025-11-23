package com.network.opmsocket.backend.chat.model;

import com.network.opmsocket.backend.user.model.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id", nullable = false)
    private AppUser addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    // Enum for status
    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        DECLINED
    }
}