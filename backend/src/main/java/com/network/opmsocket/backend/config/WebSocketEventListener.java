package com.network.opmsocket.backend.config;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // The user principal is set by your WebSocketAuthInterceptor
        if (headerAccessor.getUser() instanceof JwtAuthenticationToken principal) {
            String userId = principal.getToken().getSubject();
            updateUserStatus(userId, true);
            logger.info("User Connected: " + userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() instanceof JwtAuthenticationToken principal) {
            String userId = principal.getToken().getSubject();
            updateUserStatus(userId, false);
            logger.info("User Disconnected: " + userId);
        }
    }

    private void updateUserStatus(String userId, boolean isOnline) {
        appUserRepository.findById(userId).ifPresent(user -> {
            user.setOnline(isOnline);
            user.setLastSeen(Instant.now());
            appUserRepository.save(user);
        });
    }
}