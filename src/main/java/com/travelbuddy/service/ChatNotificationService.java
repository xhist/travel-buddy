package com.travelbuddy.service;

import com.travelbuddy.model.User;
import com.travelbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service dedicated to sending WebSocket notifications related to chat
 */
@Service
public class ChatNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    /**
     * Notify room subscribers about user presence changes
     */
    public void notifyUserPresenceChanged(Long roomId, String username, boolean online) {
        // Get user details to include in notification
        Optional<User> userOpt = userRepository.findByUsername(username);

        Map<String, Object> presenceData = new HashMap<>();
        presenceData.put("username", username);
        presenceData.put("online", online);
        presenceData.put("timestamp", LocalDateTime.now());

        userOpt.ifPresent(user -> {
            presenceData.put("userId", user.getId());
            presenceData.put("profilePicture", user.getProfilePicture());
        });

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/presence",
                presenceData
        );
    }

    /**
     * Notify room subscribers about typing status changes
     */
    public void notifyTypingStatusChanged(Long roomId, String username, boolean typing) {
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