package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock // Mock the AWS Client
    private CognitoIdentityProviderClient cognitoClient;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Manually set the @Value field for testing
        ReflectionTestUtils.setField(userService, "userPoolId", "eu-west-3_testpool");
    }

    @Test
    public void syncUser_ShouldUpdateUser_WhenUserExists() {
        // Given
        String userId = "12345";
        String newName = "Updated Name";
        String email = "updated@test.com";

        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);

        // Mock AWS Response
        AdminGetUserResponse awsResponse = AdminGetUserResponse.builder()
                .userAttributes(
                        AttributeType.builder().name("name").value(newName).build(),
                        AttributeType.builder().name("email").value(email).build()
                )
                .build();
        when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(awsResponse);

        // Mock DB
        AppUser existingUser = new AppUser(userId, "Old Name", "old@test.com");
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(existingUser);

        // When
        userService.syncUser(jwt);

        // Then
        verify(appUserRepository).save(any(AppUser.class));
    }
}