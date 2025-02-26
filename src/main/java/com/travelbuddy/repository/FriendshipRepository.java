package com.travelbuddy.repository;

import com.travelbuddy.model.Friendship;
import com.travelbuddy.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<Friendship> findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friendship f WHERE " +
            "f.user.id = :userId AND f.status = :status")
    List<Friendship> findByUserAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "f.friend.id = :userId AND f.status = :status")
    List<Friendship> findByFriendAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "((f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED')")
    List<Friendship> findAcceptedFriendships(@Param("userId") Long userId);
}