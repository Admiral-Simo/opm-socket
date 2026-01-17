package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;

    // FIX: Updated path to match 'spring.cloud.aws.cognito.user-pool-id' in your YAML
    @Value("${spring.cloud.aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public AppUser syncUser(Jwt jwt) {
        String userId = jwt.getSubject();

        // 1. Fetch the latest fresh data directly from Cognito
        Map<String, String> cognitoAttributes = getItFromCognito(userId);

        // 2. Extract values safely
        String email = cognitoAttributes.getOrDefault("email", "no-email@hidden.com");
        String name = cognitoAttributes.getOrDefault("name", "Chat User");

        // 3. Update DB
        return appUserRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUsername(name);
                    existingUser.setEmail(email);
                    return appUserRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    return appUserRepository.save(new AppUser(userId, name, email));
                });
    }

    private Map<String, String> getItFromCognito(String username) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            AdminGetUserRequest request = AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            AdminGetUserResponse response = cognitoClient.adminGetUser(request);

            return response.userAttributes().stream()
                    .collect(Collectors.toMap(AttributeType::name, AttributeType::value));

        } catch (Exception e) {
            // It is critical to see why this fails if it does
            System.err.println("Failed to fetch user from Cognito: " + e.getMessage());
            e.printStackTrace();
            return Map.of();
        }
    }
}