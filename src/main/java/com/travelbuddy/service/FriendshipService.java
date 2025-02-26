package com.travelbuddy.service;

import com.travelbuddy.model.Friendship;
import com.travelbuddy.model.FriendshipStatus;
import com.travelbuddy.model.User;
import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.repository.FriendshipRepository;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.service.interfaces.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendshipService implements IFriendService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public FriendRequestDto sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        // Check if friendship already exists
        Optional<Friendship> existingFriendship = friendshipRepository.findFriendship(senderId, receiverId);
        if (existingFriendship.isPresent()) {
            throw new IllegalStateException("Friendship already exists");
        }

        Friendship friendship = Friendship.builder()
                .user(sender)
                .friend(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        Friendship savedFriendship = friendshipRepository.save(friendship);
        return modelMapper.map(savedFriendship, FriendRequestDto.class);
    }

    @Override
    public Set<FriendRequestDto> acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (!friendship.getFriend().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to accept this friend request");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        return getPendingRequests(userId);
    }

    @Override
    public Set<FriendRequestDto> declineFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (!friendship.getFriend().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to decline this friend request");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendshipRepository.save(friendship);

        return getPendingRequests(userId);
    }

    @Override
    public Set<UserDto> removeFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findFriendship(userId, friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        if (!friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            throw new IllegalStateException("Users are not friends");
        }

        friendshipRepository.delete(friendship);
        return getUserFriends(userId);
    }

    @Override
    public Set<FriendRequestDto> getPendingRequests(Long userId) {
        return friendshipRepository.findByFriendAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(friendship -> new FriendRequestDto(
                        friendship.getId(),
                        modelMapper.map(friendship.getUser(), UserDto.class)
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UserDto> getUserFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getUser().getId().equals(userId) ?
                            friendship.getFriend() : friendship.getUser();
                    return modelMapper.map(friend, UserDto.class);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Boolean> getFriendStatus(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Friendship> friendship = friendshipRepository.findFriendship(currentUserId, user.getId());

        return Map.of(
                "status", friendship.map(f -> f.getStatus() == FriendshipStatus.ACCEPTED).orElse(false),
                "hasPendingRequest", friendship.map(f -> f.getStatus() == FriendshipStatus.PENDING).orElse(false)
        );
    }
}