package com.network.opmsocket.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.opmsocket.backend.user.model.FriendDto;
import com.network.opmsocket.backend.user.model.FriendRequestDto;
import com.network.opmsocket.backend.user.service.FriendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
public class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void sendRequest_ShouldCallService() throws Exception {
        // Arrange
        FriendRequestDto request = new FriendRequestDto();
        request.setTargetUsername("targetUser");

        // Act & Assert
        mockMvc.perform(post("/api/friends/request")
                        .with(jwt().jwt(jwt -> jwt.subject("user-id")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(friendService).sendFriendRequest("user-id", "targetUser");
    }

    @Test
    public void getFriends_ShouldReturnList() throws Exception {
        // Arrange
        List<FriendDto> friends = Arrays.asList(
                new FriendDto(1L, "friend1", "ACCEPTED"),
                new FriendDto(2L, "friend2", "ACCEPTED")
        );
        given(friendService.getFriends("user-id")).willReturn(friends);

        // Act & Assert
        mockMvc.perform(get("/api/friends")
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("friend1"));
    }

    @Test
    public void acceptRequest_ShouldCallService() throws Exception {
        mockMvc.perform(post("/api/friends/accept/123")
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isOk());

        verify(friendService).acceptFriendRequest("user-id", 123L);
    }
}