package com.network.opmsocket.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        // Spring Boot's OAuth2 Resource Server config auto-provides this bean
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Check if the message is a CONNECT command
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get the "Authorization" header from the native STOMP headers
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7); // "Bearer " is 7 chars

            try {
                // Decode and validate the JWT
                Jwt jwt = jwtDecoder.decode(token);

                // Create a Spring Security Principal (Authentication object)
                // This is the same object that @AuthenticationPrincipal resolves in REST controllers
                JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt, Collections.emptyList());

                // Set the authenticated user on the WebSocket session
                accessor.setUser(principal);
            } catch (JwtException e) {
                // Token is invalid
                throw new BadCredentialsException("Invalid JWT: " + e.getMessage());
            }
        }

        // Continue processing the message
        return message;
    }
}