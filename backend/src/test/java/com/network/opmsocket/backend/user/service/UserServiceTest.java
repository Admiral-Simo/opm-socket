package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void syncUser_ShouldCreateNewUser_WhenUserDoesNotExist() {
        // Arrange
        String userId = "keycloak-id-123";
        String email = "new@test.com";
        String username = "newuser";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(username);
        when(jwt.getClaimAsString("email")).thenReturn(email);

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Mock the save behavior to return the object passed to it
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AppUser result = userService.syncUser(jwt);

        // Assert
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(email);
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    public void syncUser_ShouldUpdateUser_WhenUserExists() {
        // Arrange
        String userId = "keycloak-id-456";
        String newEmail = "updated@test.com";

        AppUser existingUser = new AppUser(userId, "oldname", "old@test.com");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("oldname");
        when(jwt.getClaimAsString("email")).thenReturn(newEmail); // Email changed

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AppUser result = userService.syncUser(jwt);

        // Assert
        assertThat(result.getEmail()).isEqualTo(newEmail); // Should be updated
        verify(appUserRepository).save(existingUser);
    }
}