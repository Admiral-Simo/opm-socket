package com.network.opmsocket.backend.user.controller;

import com.network.opmsocket.backend.user.model.FriendDto;
import com.network.opmsocket.backend.user.model.FriendRequestDto;
import com.network.opmsocket.backend.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/request")
    public void sendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FriendRequestDto friendRequest
    ) {
        friendService.sendFriendRequest(jwt.getSubject(), friendRequest.getTargetUsername());
    }

    @PostMapping("/accept/{friendshipId}")
    public void acceptFriend(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long friendshipId
    ) {
        friendService.acceptFriendRequest(jwt.getSubject(), friendshipId);
    }

    @GetMapping("/requests")
    public List<FriendDto> getPendingRequests(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return friendService.getPendingRequests(jwt.getSubject());
    }

    @GetMapping
    public List<FriendDto> getFriends(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return friendService.getFriends(jwt.getSubject());
    }
}
