package com.network.opmsocket.backend.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    private String id; // We will use the Keycloak UUID here

    private String username;
    private boolean online;
    private Instant lastSeen;
    private String email;

    public AppUser(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.online = true;
        this.lastSeen = Instant.now();
    }
}