package com.travelbuddy.service;

import com.travelbuddy.config.RoomJoinEvent;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.ChatRoom;
import com.travelbuddy.model.User;
import com.travelbuddy.model.UserPresenceStatus;
import com.travelbuddy.repository.ChatRoomRepository;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatRoomService {
    private static final Logger log = LoggerFactory.getLogger(ChatRoomService.class);

    private final Map<String, Set<Long>> userRoomMap = new ConcurrentHashMap<>();

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatNotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gets an existing chat room or creates a new one, handling duplicate key issues
     */
    public ChatRoom getOrCreateChatRoom(Long tripId) {
        // First try to find the room
        Optional<ChatRoom> existingRoom = chatRoomRepository.findById(tripId);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            // Ensure collections are initialized
            if (room.getOnlineUsers() == null) {
                room.setOnlineUsers(new HashSet<>());
            }
            if (room.getUserStatuses() == null) {
                room.setUserStatuses(new HashSet<>());
            }
            return room;
        }

        // Room doesn't exist, create a new one
        try {
            var trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

            var chatRoom = ChatRoom.builder()
                    .trip(trip)
                    .onlineUsers(new HashSet<>())
                    .userStatuses(new HashSet<>())
                    .build();

            return chatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            // If we get an error (likely duplicate key), try to find the room again
            // This handles race conditions where multiple threads try to create the same room
            log.warn("Error creating chat room for trip {}, attempting to retrieve existing: {}", tripId, e.getMessage());
            return chatRoomRepository.findById(tripId)
                    .orElseThrow(() -> new ResourceNotFoundException("Failed to create or retrieve chat room for trip: " + tripId));
        }
    }

    public void addUserToRoom(Long roomId, String username) {
        if (username == null || username.isBlank()) {
            log.warn("Attempted to add null or blank username to room {}", roomId);
            return;
        }

        ChatRoom room = getOrCreateChatRoom(roomId);

        // Defensive initialization of collections
        if (room.getOnlineUsers() == null) {
            room.setOnlineUsers(new HashSet<>());
        }
        if (room.getUserStatuses() == null) {
            room.setUserStatuses(new HashSet<>());
        }

        // Find the user entity to get the profile picture
        User user = userRepository.findByUsername(username).orElse(null);

        room.getOnlineUsers().add(username);

        // Update or add user status
        Optional<UserPresenceStatus> existingStatus = room.getUserStatuses().stream()
                .filter(status -> status.getUsername().equals(username))
                .findFirst();

        if (existingStatus.isPresent()) {
            existingStatus.get().setLastSeen(LocalDateTime.now());
            existingStatus.get().setTyping(false);
        } else {
            UserPresenceStatus status = UserPresenceStatus.builder()
                    .username(username)
                    .lastSeen(LocalDateTime.now())
                    .typing(false)
                    .build();

            // Add user ID and profile picture if available
            if (user != null) {
                status.setUserId(user.getId());
                status.setProfilePicture(user.getProfilePicture());
            }

            room.getUserStatuses().add(status);
        }

        chatRoomRepository.save(room);

        // Track user's room in the map
        userRoomMap.computeIfAbsent(username, k -> new HashSet<>()).add(roomId);

        // Send the updated user list to all subscribers
        sendUserStatusUpdate(roomId);

        // Notify about the new user
        notificationService.notifyUserPresenceChanged(roomId, username, true);
    }

    public void removeUserFromRoom(Long roomId, String username) {
        ChatRoom room = getOrCreateChatRoom(roomId);

        // Defensive initialization of collections
        if (room.getOnlineUsers() == null) {
            room.setOnlineUsers(new HashSet<>());
        }
        if (room.getUserStatuses() == null) {
            room.setUserStatuses(new HashSet<>());
        }

        room.getOnlineUsers().remove(username);
        room.getUserStatuses().removeIf(status -> status.getUsername().equals(username));

        chatRoomRepository.save(room);

        // Remove from tracking map
        if (userRoomMap.containsKey(username)) {
            userRoomMap.get(username).remove(roomId);
            if (userRoomMap.get(username).isEmpty()) {
                userRoomMap.remove(username);
            }
        }

        // Send the updated user list to all subscribers
        sendUserStatusUpdate(roomId);

        notificationService.notifyUserPresenceChanged(roomId, username, false);
    }

    public void updateUserTypingStatus(Long roomId, String username, boolean typing) {
        ChatRoom room = getOrCreateChatRoom(roomId);

        // Defensive initialization of collections
        if (room.getOnlineUsers() == null) {
            room.setOnlineUsers(new HashSet<>());
        }
        if (room.getUserStatuses() == null) {
            room.setUserStatuses(new HashSet<>());
        }

        // Find and update user status
        Optional<UserPresenceStatus> userStatus = room.getUserStatuses().stream()
                .filter(status -> status.getUsername().equals(username))
                .findFirst();

        if (userStatus.isPresent()) {
            userStatus.get().setTyping(typing);
            userStatus.get().setLastSeen(LocalDateTime.now());
        } else {
            // User not found in statuses, add them
            User user = userRepository.findByUsername(username).orElse(null);

            UserPresenceStatus status = UserPresenceStatus.builder()
                    .username(username)
                    .lastSeen(LocalDateTime.now())
                    .typing(typing)
                    .build();

            if (user != null) {
                status.setUserId(user.getId());
                status.setProfilePicture(user.getProfilePicture());
            }

            room.getUserStatuses().add(status);
            room.getOnlineUsers().add(username);
        }

        chatRoomRepository.save(room);

        // Send an update only for typing status changes
        notificationService.notifyTypingStatusChanged(roomId, username, typing);
    }

    public Set<String> getOnlineUsers(Long roomId) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        return room.getOnlineUsers() != null ? room.getOnlineUsers() : new HashSet<>();
    }

    public Set<UserPresenceStatus> getUserStatuses(Long roomId) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        return room.getUserStatuses() != null ? room.getUserStatuses() : new HashSet<>();
    }

    /**
     * Get all rooms that a user is currently in
     */
    public Set<Long> getUserRooms(String username) {
        return userRoomMap.getOrDefault(username, Collections.emptySet());
    }

    /**
     * Handle user disconnection by removing them from all rooms they were in
     */
    public void handleUserDisconnect(String username) {
        if (username == null) {
            return;
        }

        Set<Long> rooms = new HashSet<>(getUserRooms(username));
        for (Long roomId : rooms) {
            removeUserFromRoom(roomId, username);
        }
    }

    /**
     * Send the current status of all users in this room to subscribers
     */
    private void sendUserStatusUpdate(Long roomId) {
        ChatRoom room = getOrCreateChatRoom(roomId);
        List<UserPresenceStatus> statuses = new ArrayList<>(room.getUserStatuses());

        // Sort by username for consistent ordering
        statuses.sort(Comparator.comparing(UserPresenceStatus::getUsername));

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/users",
                statuses
        );
    }

    /**
     * Handle room join events from WebSocket subscriptions
     */
    @EventListener
    public void handleRoomJoinEvent(RoomJoinEvent event) {
        log.info("User {} joining room {}", event.getUsername(), event.getRoomId());
        addUserToRoom(event.getRoomId(), event.getUsername());
    }
}