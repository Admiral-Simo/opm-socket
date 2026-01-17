package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository appUserRepository;
    // Injected Bean (Mockable!)
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${spring.cloud.aws.cognito.user-pool-id}")
    private String userPoolId;

    public AppUser syncUser(Jwt jwt) {
        String userId = jwt.getSubject();

        Map<String, String> attributes = getAttributesFromCognito(userId);

        String email = attributes.getOrDefault("email", "no-email@hidden.com");
        String name = attributes.get("name");

        if (name == null || name.isEmpty()) {
            name = attributes.getOrDefault("cognito:username", "Chat User");
        }

        final String finalName = name;
        final String finalEmail = email;

        return appUserRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUsername(finalName);
                    existingUser.setEmail(finalEmail);
                    return appUserRepository.save(existingUser);
                })
                .orElseGet(() -> appUserRepository.save(new AppUser(userId, finalName, finalEmail)));
    }

    private Map<String, String> getAttributesFromCognito(String userId) {
        try {
            AdminGetUserRequest request = AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userId)
                    .build();

            AdminGetUserResponse response = cognitoClient.adminGetUser(request);

            return response.userAttributes().stream()
                    .collect(Collectors.toMap(AttributeType::name, AttributeType::value));

        } catch (Exception e) {
            log.error("Error fetching user from Cognito: {}", e.getMessage());
            return Map.of();
        }
    }
}