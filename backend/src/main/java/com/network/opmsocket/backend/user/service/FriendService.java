package com.network.opmsocket.backend.user.service;

import com.network.opmsocket.backend.chat.model.Friendship;
import com.network.opmsocket.backend.chat.repository.AppUserRepository;
import com.network.opmsocket.backend.chat.repository.FriendshipRepository;
import com.network.opmsocket.backend.user.model.AppUser;
import com.network.opmsocket.backend.user.model.FriendDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final AppUserRepository appUserRepository;

    private final FriendshipRepository friendshipRepository;

    @Transactional
    public void sendFriendRequest(String requesterId, String targetUsername) {
        AppUser requester = appUserRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester user not found."));

        AppUser target = appUserRepository.findByUsername(targetUsername);
        if (target == null) {
            throw new RuntimeException("Target user not found.");
        }

        if (requester.getId().equals(target.getId())) {
            throw new RuntimeException("You cannot add yourself as a friend.");
        }

        if (friendshipRepository.findFriendshipBetween(requester, target).isPresent()) {
            throw new RuntimeException("Friendship request already exists.");
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(target);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptFriendRequest(String userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request is not found."));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to accept this friend request.");
        }

        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public List<FriendDto> getPendingRequests(String userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return friendshipRepository
                        .findByAddresseeAndStatus(user, Friendship.FriendshipStatus.PENDING)
                .stream()
                .map(f -> {
                    return new FriendDto(
                            f.getId(),
                            f.getRequester().getUsername(),
                            f.getStatus().toString()
                    );
                })
                .toList();
    }

    public List<FriendDto> getFriends(String userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return friendshipRepository
                .findByAddresseeAndStatus(user, Friendship.FriendshipStatus.ACCEPTED)
                .stream().map(f -> {
                    AppUser other = f.getRequester().getId().equals(userId) ? f.getAddressee() : f.getRequester();
                    return new FriendDto(
                            f.getId(),
                            other.getUsername(),
                            Friendship.FriendshipStatus.ACCEPTED.toString()
                    );
                })
                .toList();
    }
}
