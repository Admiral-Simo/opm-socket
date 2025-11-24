package com.network.opmsocket.backend.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@Setter
@Getter
public class FriendDto {
    private Long id;
    private String username;
    private String status;

    private boolean online;
    private Instant lastSeen;
}
