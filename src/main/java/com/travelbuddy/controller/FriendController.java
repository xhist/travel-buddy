package com.travelbuddy.controller;

import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.FriendRequest;
import com.travelbuddy.service.interfaces.IFriendService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@Slf4j
public class FriendController {

    @Autowired
    private IFriendService friendService;

    @Autowired
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<Set<UserDto>> getFriendsList(@PathVariable Long userId) {
        final var friends = friendService.getUserFriends(userId)
                .stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toSet());
        return ResponseEntity.ok(friends);
    }

    // Send a friend request
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{userId}/request/{receiverId}")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(@PathVariable Long userId, @PathVariable Long receiverId) {
        FriendRequestDto friendRequest = friendService.sendFriendRequest(userId, receiverId);
        return ResponseEntity.ok(friendRequest);
    }

    // Accept a friend request
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{userId}/accept/{requestId}")
    public ResponseEntity<Set<FriendRequestDto>> acceptFriendRequest(@PathVariable Long requestId) {
        final var pendingRequests = friendService.acceptFriendRequest(requestId);
        return ResponseEntity.ok(pendingRequests);
    }

    // Decline a friend request
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{userId}/decline/{requestId}")
    public ResponseEntity<Set<FriendRequestDto>> declineFriendRequest(@PathVariable Long requestId) {
        final var pendingRequests = friendService.declineFriendRequest(requestId);
        return ResponseEntity.ok(pendingRequests);
    }

    // Remove a friend
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/user/{userId}/friends/{friendId}")
    public ResponseEntity<Set<UserDto>> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        final var friends = friendService.removeFriend(userId, friendId)
                .stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toSet());
        return ResponseEntity.ok(friends);
    }

    // Get pending friend requests (for the current user)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{userId}/pending")
    public ResponseEntity<Set<FriendRequestDto>> getPendingRequests(@PathVariable Long userId) {
        Set<FriendRequestDto> pending = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(pending);
    }
}
