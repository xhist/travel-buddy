package com.travelbuddy.service;

import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.ChatRoom;
import com.travelbuddy.model.UserPresenceStatus;
import com.travelbuddy.repository.ChatRoomRepository;
import com.travelbuddy.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatRoom getOrCreateChatRoom(Long tripId) {
        return chatRoomRepository.findById(tripId)
                .orElseGet(() -> {
                    var trip = tripRepository.findById(tripId)
                            .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
                    var chatRoom = ChatRoom.builder()
                            .trip(trip)
                            .build();
                    return chatRoomRepository.save(chatRoom);
                });
    }

    public void addUserToRoom(Long roomId, String username) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        room.getOnlineUsers().add(username);

        UserPresenceStatus status = UserPresenceStatus.builder()
                .username(username)
                .lastSeen(LocalDateTime.now())
                .typing(false)
                .build();
        room.getUserStatuses().add(status);

        chatRoomRepository.save(room);
        notifyUserPresenceChanged(roomId, username, true);
    }

    public void removeUserFromRoom(Long roomId, String username) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        room.getOnlineUsers().remove(username);
        room.getUserStatuses().removeIf(status -> status.getUsername().equals(username));

        chatRoomRepository.save(room);
        notifyUserPresenceChanged(roomId, username, false);
    }

    public void updateUserTypingStatus(Long roomId, String username, boolean typing) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        room.getUserStatuses().stream()
                .filter(status -> status.getUsername().equals(username))
                .findFirst()
                .ifPresent(status -> status.setTyping(typing));

        chatRoomRepository.save(room);
        notifyTypingStatusChanged(roomId, username, typing);
    }

    public Set<String> getOnlineUsers(Long roomId) {
        return getOrCreateChatRoom(roomId).getOnlineUsers();
    }

    public Set<UserPresenceStatus> getUserStatuses(Long roomId) {
        return getOrCreateChatRoom(roomId).getUserStatuses();
    }

    private void notifyUserPresenceChanged(Long roomId, String username, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/presence",
                Map.of(
                        "username", username,
                        "online", online,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    private void notifyTypingStatusChanged(Long roomId, String username, boolean typing) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/typing",
                Map.of(
                        "username", username,
                        "typing", typing,
                        "timestamp", LocalDateTime.now()
                )
        );
    }
}