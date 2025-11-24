package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.model.Friendship;
import com.network.opmsocket.backend.chat.model.Friendship.FriendshipStatus;
import com.network.opmsocket.backend.chat.repository.FriendshipRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static com.network.opmsocket.backend.chat.model.Friendship.FriendshipStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class FriendshipRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Shared test data fields
    private AppUser me;
    private AppUser other;
    private AppUser friend1;
    private AppUser friend2;
    private AppUser existingFriend;

    @BeforeEach
    public void setUp() {
        me = persistUser("id-1", "me@test.com");
        other = persistUser("id-2", "other@test.com");
        friend1 = persistUser("id-3", "friend1@test.com");
        friend2 = persistUser("id-4", "friend2@test.com");
        existingFriend = persistUser("id-5", "oldfriend@test.com");
    }

    @Test
    public void should_findFriendship_when_itExists() {
        // Arrange
        persistFriendship(me, other, PENDING);

        // Act
        Optional<Friendship> result = friendshipRepository.findFriendshipBetween(other, me);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getRequester()).isEqualTo(me);
        assertThat(result.get().getAddressee()).isEqualTo(other);
    }

    @Test
    public void should_separatePendingAndAccepted_correctly() {
        // Arrange
        persistFriendship(friend1, me, PENDING);  // Request from friend1
        persistFriendship(friend2, me, PENDING);  // Request from friend2
        persistFriendship(existingFriend, me, ACCEPTED); // Already friends

        // Act
        List<Friendship> pending = friendshipRepository.findByAddresseeAndStatus(me, PENDING);
        List<Friendship> accepted = friendshipRepository.findByAddresseeAndStatus(me, ACCEPTED);

        // Assert
        assertThat(pending)
                .hasSize(2)
                .extracting(f -> f.getRequester().getUsername())
                .containsExactlyInAnyOrder("friend1@test.com", "friend2@test.com");

        assertThat(accepted)
                .hasSize(1)
                .extracting(f -> f.getRequester().getUsername())
                .containsExactly("oldfriend@test.com");
    }

    @Test
    public void should_findAllFriendships_involvingUser() {
        // Arrange
        // Case 1: Me -> Friend1 (I am the Requester)
        persistFriendship(me, friend1, ACCEPTED);

        // Case 2: Friend2 -> Me (I am the Addressee)
        persistFriendship(friend2, me, PENDING);

        // Case 3: Friend1 -> Friend2 (Irrelevant to 'me')
        persistFriendship(friend1, friend2, ACCEPTED);
        // Act
        List<Friendship> result = friendshipRepository.findByRequesterOrAddressee(me, me);

        // Assert
        assertThat(result).hasSize(2);

        // Verify
        assertThat(result)
                .extracting(f -> {
                    // Helper logic to extract the *other* person's username
                    if (f.getRequester().equals(me)) {
                        return f.getAddressee().getUsername();
                    } else {
                        return f.getRequester().getUsername();
                    }
                })
                .containsExactlyInAnyOrder("friend1@test.com", "friend2@test.com");
    }

    private AppUser persistUser(String id, String email) {
        AppUser user = new AppUser(id, email, email);
        return entityManager.persistAndFlush(user);
    }

    private void persistFriendship(AppUser req, AppUser addr, FriendshipStatus status) {
        Friendship f = new Friendship();
        f.setRequester(req);
        f.setAddressee(addr);
        f.setStatus(status);
        entityManager.persistAndFlush(f);
    }
}