package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.FriendRequest;
import com.travelbuddy.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IFriendService {
    FriendRequestDto sendFriendRequest(Long senderId, Long receiverId);
    Set<FriendRequestDto> acceptFriendRequest(Long friendshipId, Long userId);
    Set<FriendRequestDto> declineFriendRequest(Long friendshipId, Long userId);
    Set<UserDto> removeFriend(Long userId, Long friendId);
    Set<FriendRequestDto> getPendingRequests(Long receiverId);
    Set<UserDto> getUserFriends(Long userId);
    Map<String, Boolean> getFriendStatus(String username, Long userId);
}
