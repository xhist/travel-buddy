package com.travelbuddy.controller;

import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.FriendshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/friends")
@Slf4j
public class FriendController {

    @Autowired
    private FriendshipService friendshipService;

    @PreAuthorize("@securityEvaluator.isSameUser(#userId)")
    @GetMapping("/{userId}")
    public ResponseEntity<Set<UserDto>> getFriendsList(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.getUserFriends(userId));
    }

    @PreAuthorize("@securityEvaluator.isSameUser(#senderId)")
    @PostMapping("/{senderId}/request/{receiverId}")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(friendshipService.sendFriendRequest(senderId, receiverId));
    }

    @PreAuthorize("@securityEvaluator.isSameUser(#userId)")
    @PostMapping("/{userId}/accept/{requestId}")
    public ResponseEntity<Set<FriendRequestDto>> acceptFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(requestId, userId));
    }

    @PreAuthorize("@securityEvaluator.isSameUser(#userId)")
    @PostMapping("/{userId}/decline/{requestId}")
    public ResponseEntity<Set<FriendRequestDto>> declineFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(friendshipService.declineFriendRequest(requestId, userId));
    }

    @PreAuthorize("@securityEvaluator.isSameUser(#userId)")
    @DeleteMapping("/user/{userId}/friends/{friendId}")
    public ResponseEntity<Set<UserDto>> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        return ResponseEntity.ok(friendshipService.removeFriend(userId, friendId));
    }

    @PreAuthorize("@securityEvaluator.isSameUser(#userId)")
    @GetMapping("/{userId}/pending")
    public ResponseEntity<Set<FriendRequestDto>> getPendingRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/status/{username}")
    public ResponseEntity<Map<String, Boolean>> getFriendStatus(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(friendshipService.getFriendStatus(username, principal.getId()));
    }
}