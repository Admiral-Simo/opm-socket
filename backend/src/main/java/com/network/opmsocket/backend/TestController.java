package com.network.opmsocket.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String sayHello(@AuthenticationPrincipal Jwt jwt) {
        // By default, Spring Security protects ALL endpoints.
        // If you get here, the token was valid.

        // We can inspect the token:
        String username = jwt.getClaimAsString("preferred_username"); // This will be "user1"
        String userId = jwt.getSubject(); // This is the Keycloak User ID

        return "Hello, " + username + "! Your User ID is: " + userId;
    }
}