package com.network.opmsocket.backend.user.controller;

import com.network.opmsocket.backend.user.model.AppUser;
import com.network.opmsocket.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sync")
    public AppUser sync(@AuthenticationPrincipal Jwt jwt) {
        return userService.syncUser(jwt);
    }
}
