package com.network.opmsocket.backend.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ChatSocketController {

    /**
     * Handles incoming chat messages from any authenticated user.
     *
     * @param chatMessage The incoming message payload (e.g., {"content": "Hello!"})
     * @param jwt         The authenticated user principal, injected by Spring Security
     * (thanks to our WebSocketAuthInterceptor).
     * @return The message to be broadcast to all subscribers.
     */
    @MessageMapping("/chat.sendMessage") // Listens to /app/chat.sendMessage
    @SendTo("/topic/public") // Broadcasts to /topic/public
    public PublicMessage sendMessage(
            @Payload ChatMessage chatMessage,
            @AuthenticationPrincipal Jwt jwt) {

        // Get the username from the authenticated JWT
        // We use "preferred_username" as that's a standard Keycloak claim
        String username = jwt.getClaimAsString("preferred_username");

        // Sanitize the input content to prevent XSS
        String sanitizedContent = HtmlUtils.htmlEscape(chatMessage.getContent());

        // Create the public message to be broadcast
        return new PublicMessage(username, sanitizedContent);
    }
}