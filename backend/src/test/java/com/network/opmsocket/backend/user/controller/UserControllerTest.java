package com.network.opmsocket.backend.user.controller;

import com.network.opmsocket.backend.user.model.AppUser;
import com.network.opmsocket.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder; // Required for SecurityConfig

    @Test
    public void syncUser_ShouldReturnSyncedUser() throws Exception {
        // Arrange
        AppUser mockUser = new AppUser("uid-1", "simoo", "simoo@test.com");
        when(userService.syncUser(any(Jwt.class))).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/sync")
                        .with(jwt().jwt(builder -> builder.subject("uid-1")))) // Simulate auth
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("simoo"))
                .andExpect(jsonPath("$.email").value("simoo@test.com"));
    }

    @Test
    public void syncUser_Unauthenticated_ShouldFail() throws Exception {
        mockMvc.perform(post("/api/users/sync"))
                .andExpect(status().isForbidden());
    }
}