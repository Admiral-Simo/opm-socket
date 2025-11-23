package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AppUserRepository appUserRepository;
    public AppUser syncUser(Jwt jwt) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");


        return appUserRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUsername(username);
                    existingUser.setEmail(email);
                    return appUserRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    return appUserRepository.save(new AppUser(userId, username, email));
                });
    }
}
