package com.network.opmsocket.backend.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.opmsocket.backend.chat.model.ChatMessageDto;
import com.network.opmsocket.backend.chat.model.PublicMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(converter);
    }

    @Test
    public void verifyPublicChatFlow() throws Exception {
        // 1. Mock the Authentication
        Jwt mockJwt = mock(Jwt.class);

        // Define the test user
        String testUserId = "test-user-id";
        String testUsername = "IntegrationTestUser";

        when(mockJwt.getSubject()).thenReturn(testUserId);

        // --- FIX: Use the claim that ChatSocketController actually looks for! ---
        // The controller looks for "name" or "cognito:username" as a fallback
        when(mockJwt.getClaimAsString("name")).thenReturn(testUsername);
        when(mockJwt.getClaimAsString("cognito:username")).thenReturn(testUsername);

        when(jwtDecoder.decode("my-fake-token")).thenReturn(mockJwt);

        // 2. Define the WebSocket URL
        String url = "ws://localhost:" + port + "/ws";

        // 3. Create Session Handler
        BlockingQueue<PublicMessageDto> blockingQueue = new LinkedBlockingDeque<>();
        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/public", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return PublicMessageDto.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        blockingQueue.offer((PublicMessageDto) payload);
                    }
                });
            }
        };

        // 4. Connect
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer my-fake-token");

        StompSession session = stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler)
                .get(5, TimeUnit.SECONDS); // Increased timeout for CI/slow environments

        // 5. Send a message
        Thread.sleep(500); // Give the subscription a moment to register

        ChatMessageDto chatMessage = new ChatMessageDto();
        chatMessage.setContent("Hello Integration Test!");

        session.send("/app/chat.sendMessage", chatMessage);

        // 6. Wait for the message
        PublicMessageDto receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);

        // 7. Assertions
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo("Hello Integration Test!");
        assertThat(receivedMessage.getSenderName()).isEqualTo(testUsername);
        assertThat(receivedMessage.getTimestamp()).isNotNull();
    }
}