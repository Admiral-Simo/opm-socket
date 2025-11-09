package com.network.opmsocket.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // --- NEW: Inject our auth interceptor ---
    private final WebSocketAuthInterceptor authInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }
    // ----------------------------------------

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. Broker Prefix:
        // This is the prefix for messages FROM the server TO the client (broadcasts).
        // Clients will subscribe to destinations like "/topic/public".
        registry.enableSimpleBroker("/topic");

        // 2. Application Prefix:
        // This is the prefix for messages FROM the client TO the server.
        // A client will send a message to a destination like "/app/chat.sendMessage".
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Handshake Endpoint:
        // This is the initial HTTP endpoint for the WebSocket handshake.
        // The Next.js client will connect to "http://localhost:8080/ws".
        registry.addEndpoint("/ws")
                // 2. CORS:
                // Allow our Next.js client to connect.
                .setAllowedOrigins("http://localhost:3000")
                // 3. SockJS Fallback:
                // Enable SockJS for browsers that don't support native WebSockets.
                .withSockJS();
    }

    // --- NEW: Register the interceptor ---
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add our interceptor to the inbound channel
        // It will now run "preSend" on every message from the client
        registration.interceptors(authInterceptor);
    }
    // ------------------------------------
}