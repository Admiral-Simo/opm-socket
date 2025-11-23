package com.network.opmsocket.backend.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class FriendDto {
    private Long id;
    private String username;
    private String status;
}
