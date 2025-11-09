package com.network.opmsocket.backend.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    /**
     * Handles public chat messages.
     *
     * @param message   The incoming message payload, mapped from JSON.
     * @param principal The authenticated user, injected by Spring Security from the
     * WebSocket session.
     * @return A PublicMessage object that will be broadcast to all subscribers.
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public PublicMessage sendMessage(
            @Payload ChatMessage message,
            // --- THIS IS THE FIX ---
            // Ask for the exact Principal object we set in the interceptor
            @AuthenticationPrincipal JwtAuthenticationToken principal) {

        // Now, we can safely get the Jwt and its claims
        Jwt jwt = principal.getToken();
        String username = jwt.getClaimAsString("preferred_username");

        if (username == null) {
            username = "Anonymous";
        }

        // Create the outgoing message object
        return new PublicMessage(username, message.getContent());
    }
}