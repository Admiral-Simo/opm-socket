package com.network.opmsocket.backend.chat.model;

import jakarta.persistence.*;
import java.time.Instant;

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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getRequester() { return requester; }
    public void setRequester(AppUser requester) { this.requester = requester; }

    public AppUser getAddressee() { return addressee; }
    public void setAddressee(AppUser addressee) { this.addressee = addressee; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
}