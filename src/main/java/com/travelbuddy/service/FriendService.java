package com.travelbuddy.service;

import com.travelbuddy.dto.FriendRequestDto;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.FriendRequest;
import com.travelbuddy.model.User;
import com.travelbuddy.repository.FriendRequestRepository;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IFriendService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendService implements IFriendService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public FriendRequestDto sendFriendRequest(Long senderId, Long receiverId) {
        if (friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId).isPresent()) {
            throw new RuntimeException("Friend request already sent.");
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        FriendRequest saved = friendRequestRepository.save(request);
        log.info("Friend request sent from {} to {}", sender.getUsername(), receiver.getUsername());
        return modelMapper.map(saved, FriendRequestDto.class);
    }

    @Override
    @Transactional
    public Set<FriendRequestDto> acceptFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        User sender = request.getSender();
        User receiver = request.getReceiver();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);
        friendRequestRepository.delete(request);
        userRepository.saveAndFlush(sender);
        userRepository.saveAndFlush(receiver);
        log.info("Friend request {} accepted", requestId);
        return friendRequestRepository.findByReceiverId(request.getReceiver().getId())
                .stream().map(dbRequest -> modelMapper.map(dbRequest, FriendRequestDto.class)).collect(Collectors.toSet());
    }

    @Override
    public Set<FriendRequestDto> declineFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        friendRequestRepository.delete(request);
        log.info("Friend request {} declined", requestId);
        return friendRequestRepository.findByReceiverId(request.getReceiver().getId()).stream()
                .map(friendRequest ->
                        new FriendRequestDto(friendRequest.getId(),
                                modelMapper.map(friendRequest.getSender(), UserDto.class)))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Set<UserDto> removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));
        if (user.getFriends().remove(friend)) {
            friend.getFriends().remove(user);
            userRepository.save(user);
            userRepository.save(friend);
            log.info("Friend {} removed from user {}", friend.getUsername(), user.getUsername());
        } else {
            throw new RuntimeException("Users are not friends.");
        }
        return user.getFriends().stream()
                .map(userFriend -> modelMapper.map(userFriend, UserDto.class)).collect(Collectors.toSet());
    }

    @Override
    public Set<FriendRequestDto> getPendingRequests(Long receiverId) {
        return friendRequestRepository.findByReceiverId(receiverId).stream()
                .map(friendRequest ->
                    new FriendRequestDto(friendRequest.getId(),
                            modelMapper.map(friendRequest.getSender(), UserDto.class)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UserDto> getUserFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFriends().stream()
                .map(friend -> modelMapper.map(friend, UserDto.class)).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Map<String, Boolean> getFriendStatus(String username, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Map.of(
                "status", currentUser.getFriends().stream()
                        .anyMatch(friend -> friend.getUsername().equals(username)),
                "hasPendingRequest", friendRequestRepository.findByReceiverId(currentUserId).stream()
                        .anyMatch(request -> request.getSender().getUsername().equals(username))
        );
    }
}
