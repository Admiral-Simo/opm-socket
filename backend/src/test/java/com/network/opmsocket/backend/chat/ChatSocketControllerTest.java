package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.controller.ChatSocketController;
import com.network.opmsocket.backend.chat.model.ChatMessageDto;
import com.network.opmsocket.backend.chat.model.Message;
import com.network.opmsocket.backend.chat.model.PublicMessageDto;
import com.network.opmsocket.backend.chat.repository.AppUserRepository; // Import this
import com.network.opmsocket.backend.chat.repository.MessageRepository;
import com.network.opmsocket.backend.user.model.AppUser; // Import this
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatSocketControllerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock // <--- Fix 1: Add the missing Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ChatSocketController chatSocketController;

    @Test
    public void sendMessage_ShouldSaveAndReturnPublicMessage() {
        // Given
        String userId = "user-123";
        String username = "TestUser";
        ChatMessageDto inputMessage = new ChatMessageDto();
        inputMessage.setContent("Hello World");

        // Mock JWT
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId); // Return UUID
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt);

        // Mock AppUserRepository to return a user (Fix 2)
        AppUser mockUser = new AppUser(userId, username, "test@mail.com");
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock MessageRepository
        Message savedMessage = new Message();
        savedMessage.setSenderName(username);
        savedMessage.setContent("Hello World");
        savedMessage.setTimestamp(Instant.now());
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // When
        PublicMessageDto result = chatSocketController.sendMessage(inputMessage, principal);

        // Then
        assertEquals("Hello World", result.getContent());
        assertEquals(username, result.getSenderName());
    }
}