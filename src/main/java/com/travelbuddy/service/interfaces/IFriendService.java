package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.FriendRequest;

import java.util.List;

public interface IFriendService {
    FriendRequest sendFriendRequest(Long senderId, Long receiverId);
    void acceptFriendRequest(Long requestId);
    void removeFriend(Long userId, Long friendId);
    List<FriendRequest> getPendingRequests(Long receiverId);
}
