package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.model.Friendship;
import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.chat.repository.FriendshipRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendService friendService;

    private AppUser me;
    private AppUser target;

    @BeforeEach
    public void setup() {
        me = new AppUser("id-me", "me", "me@test.com");
        target = new AppUser("id-target", "target", "target@test.com");
    }

    @Test
    public void sendFriendRequest_ShouldSavePendingFriendship_WhenValid() {
        // Arrange
        when(appUserRepository.findById("id-me")).thenReturn(Optional.of(me));
        when(appUserRepository.findByUsername("target")).thenReturn(target);
        when(friendshipRepository.findFriendshipBetween(me, target)).thenReturn(Optional.empty());

        // Act
        friendService.sendFriendRequest("id-me", "target");

        // Assert
        verify(friendshipRepository).save(argThat(f ->
                f.getRequester().equals(me) &&
                        f.getAddressee().equals(target) &&
                        f.getStatus() == Friendship.FriendshipStatus.PENDING
        ));
    }

    @Test
    public void sendFriendRequest_ShouldThrow_WhenAddingSelf() {
        when(appUserRepository.findById("id-me")).thenReturn(Optional.of(me));
        when(appUserRepository.findByUsername("me")).thenReturn(me);

        assertThatThrownBy(() -> friendService.sendFriendRequest("id-me", "me"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You cannot add yourself as a friend.");

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    public void sendFriendRequest_ShouldThrow_WhenAlreadyFriends() {
        when(appUserRepository.findById("id-me")).thenReturn(Optional.of(me));
        when(appUserRepository.findByUsername("target")).thenReturn(target);
        when(friendshipRepository.findFriendshipBetween(me, target))
                .thenReturn(Optional.of(new Friendship())); // Exists

        assertThatThrownBy(() -> friendService.sendFriendRequest("id-me", "target"))
                .isInstanceOf(RuntimeException.class);

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    public void acceptFriendRequest_ShouldUpdateStatus_WhenAuthorized() {
        // Arrange
        Friendship request = new Friendship();
        request.setRequester(target);
        request.setAddressee(me); // "me" is the one accepting
        request.setStatus(Friendship.FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(request));

        // Act
        friendService.acceptFriendRequest("id-me", 1L);

        // Assert
        assertThat(request.getStatus()).isEqualTo(Friendship.FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).save(request);
    }

    @Test
    public void acceptFriendRequest_ShouldThrow_WhenNotAddressee() {
        // Arrange
        Friendship request = new Friendship();
        request.setRequester(me); // "me" sent it, so "me" shouldn't accept it
        request.setAddressee(target);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThatThrownBy(() -> friendService.acceptFriendRequest("id-me", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not authorized");
    }
}