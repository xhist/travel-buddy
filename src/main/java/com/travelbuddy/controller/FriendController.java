package com.travelbuddy.controller;

import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.model.FriendRequest;
import com.travelbuddy.service.interfaces.IFriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
@Slf4j
public class FriendController {

    @Autowired
    private IFriendService friendService;

    // Send a friend request
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@Valid @RequestBody FriendRequestDto requestDto) {
        FriendRequest friendRequest = friendService.sendFriendRequest(requestDto.getSenderId(), requestDto.getReceiverId());
        return ResponseEntity.ok(friendRequest);
    }

    // Accept a friend request
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        friendService.acceptFriendRequest(requestId);
        return ResponseEntity.ok("Friend request accepted");
    }

    // Remove a friend
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/user/{userId}/friends/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok("Friend removed");
    }

    // Get pending friend requests (for the current user)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/users/{userId}/pending")
    public ResponseEntity<?> getPendingRequests(@PathVariable Long userId) {
        List<FriendRequest> pending = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(pending);
    }
}
