package com.network.opmsocket.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.opmsocket.backend.chat.model.Friendship;
import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.chat.repository.FriendshipRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import com.network.opmsocket.backend.user.model.FriendRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back DB changes after each test
public class SocialFeatureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // We mock the JwtDecoder so we don't need a real Keycloak server running
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUp() {
        // Clear DB before each test to ensure a clean slate
        friendshipRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    public void testUserSync_ShouldSaveUserToDatabase() throws Exception {
        // 1. Call the Sync endpoint as "new-user"
        mockMvc.perform(post("/api/users/sync")
                        .with(jwt().jwt(jwt -> {
                            jwt.subject("keycloak-id-1");
                            jwt.claim("preferred_username", "simoo");
                            jwt.claim("email", "simoo@test.com");
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("simoo"));

        // 2. Verify directly in the Database
        AppUser savedUser = appUserRepository.findById("keycloak-id-1").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("simoo");
        assertThat(savedUser.getEmail()).isEqualTo("simoo@test.com");
    }

    @Test
    public void testFullFriendshipFlow() throws Exception {
        // --- STEP 1: Setup Users (Simulate Sync) ---
        // We manually save them to the repo to simulate them having already logged in
        AppUser alice = new AppUser("id-alice", "alice", "alice@test.com");
        AppUser bob = new AppUser("id-bob", "bob", "bob@test.com");
        appUserRepository.save(alice);
        appUserRepository.save(bob);

        // --- STEP 2: Alice sends friend request to Bob ---
        FriendRequestDto request = new FriendRequestDto();
        request.setTargetUsername("bob");

        mockMvc.perform(post("/api/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(jwt -> jwt.subject("id-alice")))) // Alice is the sender
                .andExpect(status().isOk());

        // Verify DB state: 1 Pending request
        List<Friendship> friendships = friendshipRepository.findAll();
        assertThat(friendships).hasSize(1);
        assertThat(friendships.get(0).getStatus()).isEqualTo(Friendship.FriendshipStatus.PENDING);
        assertThat(friendships.get(0).getRequester().getId()).isEqualTo("id-alice");

        // --- STEP 3: Bob checks his pending requests ---
        mockMvc.perform(get("/api/friends/requests")
                        .with(jwt().jwt(jwt -> jwt.subject("id-bob")))) // Bob is the viewer
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("alice")); // Bob sees request from Alice

        // --- STEP 4: Bob accepts the request ---
        Long friendshipId = friendships.get(0).getId();

        mockMvc.perform(post("/api/friends/accept/" + friendshipId)
                        .with(jwt().jwt(jwt -> jwt.subject("id-bob")))) // Bob accepts
                .andExpect(status().isOk());

        // --- STEP 5: Verify Friendship is now Official ---

        // Verify DB State
        Friendship updatedFriendship = friendshipRepository.findById(friendshipId).orElseThrow();
        assertThat(updatedFriendship.getStatus()).isEqualTo(Friendship.FriendshipStatus.ACCEPTED);

        // Verify Alice sees Bob as a friend
        mockMvc.perform(get("/api/friends")
                        .with(jwt().jwt(jwt -> jwt.subject("id-alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("bob"));

        // Verify Bob sees Alice as a friend
        mockMvc.perform(get("/api/friends")
                        .with(jwt().jwt(jwt -> jwt.subject("id-bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    public void testPreventDuplicateFriendRequests() throws Exception {
        // 1. Setup
        AppUser alice = new AppUser("id-alice", "alice", "alice@test.com");
        AppUser bob = new AppUser("id-bob", "bob", "bob@test.com");
        appUserRepository.save(alice);
        appUserRepository.save(bob);

        // 2. Alice sends request to Bob
        FriendRequestDto request = new FriendRequestDto();
        request.setTargetUsername("bob");

        mockMvc.perform(post("/api/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(jwt -> jwt.subject("id-alice"))))
                .andExpect(status().isOk());

        // 3. Alice tries to send it AGAIN (Should fail)
        // Expecting 500 Internal Server Error (due to RuntimeException in service)
        // You could map this to 400 Bad Request with an @ExceptionHandler for better API design
        mockMvc.perform(post("/api/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(jwt -> jwt.subject("id-alice"))))
                .andExpect(status().isConflict());

        // Verify only 1 record exists
        assertThat(friendshipRepository.count()).isEqualTo(1);
    }
}