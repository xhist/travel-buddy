package com.travelbuddy.service;

import com.travelbuddy.model.FriendRequest;
import com.travelbuddy.model.User;
import com.travelbuddy.repository.FriendRequestRepository;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IFriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class FriendService implements IFriendService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public FriendRequest sendFriendRequest(Long senderId, Long receiverId) {
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
                .accepted(false)
                .build();
        FriendRequest saved = friendRequestRepository.save(request);
        log.info("Friend request sent from {} to {}", sender.getUsername(), receiver.getUsername());
        return saved;
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        request.setAccepted(true);
        User sender = request.getSender();
        User receiver = request.getReceiver();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);
        friendRequestRepository.save(request);
        log.info("Friend request {} accepted", requestId);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
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
    }

    @Override
    public List<FriendRequest> getPendingRequests(Long receiverId) {
        return friendRequestRepository.findByReceiverIdAndAcceptedFalse(receiverId);
    }
}
