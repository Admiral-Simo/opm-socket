package com.network.opmsocket.backend.chat.repository;

import com.network.opmsocket.backend.user.model.AppUser;
import com.network.opmsocket.backend.chat.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Check if a friendship already exists between two users (in either direction)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findFriendshipBetween(@Param("user1") AppUser user1, @Param("user2") AppUser user2);

    // Find all friendships for a specific user
    List<Friendship> findByRequesterOrAddressee(AppUser requester, AppUser addressee);

    // Find specific pending requests for a user (where they are the addressee)
    List<Friendship> findByAddresseeAndStatus(AppUser addressee, Friendship.FriendshipStatus status);
}