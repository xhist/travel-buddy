package com.travelbuddy.repository;

import com.travelbuddy.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndAcceptedFalse(Long receiverId);
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
