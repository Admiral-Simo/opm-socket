package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatSocketControllerTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatSocketController chatSocketController;

    @Test
    public void sendMessage_ShouldSaveAndReturnPublicMessage() {
        // 1. Arrange
        String testUser = "simoo";
        String testContent = "Hello WebSocket";

        // Mock the input message
        ChatMessage incomingMessage = new ChatMessage();
        incomingMessage.setContent(testContent);

        // Mock the Security Principal (JWT)
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(testUser);
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt);

        // Mock the repository save behavior
        Message savedMessage = new Message();
        savedMessage.setSenderName(testUser);
        savedMessage.setContent(testContent);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // 2. Act
        PublicMessage result = chatSocketController.sendMessage(incomingMessage, principal);

        // 3. Assert
        assertEquals(testUser, result.getSenderName());
        assertEquals(testContent, result.getContent());

        // Verify the repository was actually called
        verify(messageRepository, times(1)).save(any(Message.class));
    }
}