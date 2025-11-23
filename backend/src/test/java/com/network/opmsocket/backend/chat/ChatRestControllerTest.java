package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.controller.ChatRestController;
import com.network.opmsocket.backend.chat.model.Message;
import com.network.opmsocket.backend.chat.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatRestController.class)
public class ChatRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageRepository messageRepository;

    // Mock JwtDecoder is required because SecurityConfig is loaded
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    public void getPublicChatHistory_ShouldReturnMessages() throws Exception {
        // 1. Arrange: Mock the database data
        Message msg1 = new Message();
        msg1.setSenderName("user1");
        msg1.setContent("Hello");
        msg1.setTimestamp(Instant.now());

        Message msg2 = new Message();
        msg2.setSenderName("user2");
        msg2.setContent("Hi there");
        msg2.setTimestamp(Instant.now());

        List<Message> messages = Arrays.asList(msg1, msg2);

        given(messageRepository.findAll()).willReturn(messages);

        // 2. Act & Assert: Perform GET request with a mock JWT
        mockMvc.perform(get("/api/chat/public/history")
                        .with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser")))) // Simulate logged-in user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderName").value("user1"))
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].senderName").value("user2"))
                .andExpect(jsonPath("$[1].content").value("Hi there"));
    }

    @Test
    public void getPublicChatHistory_Unauthenticated_ShouldFail() throws Exception {
        // Verify that requests without a token are rejected
        mockMvc.perform(get("/api/chat/public/history"))
                .andExpect(status().isUnauthorized());
    }
}