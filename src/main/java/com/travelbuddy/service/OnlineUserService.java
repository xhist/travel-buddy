package com.travelbuddy.service;

import com.travelbuddy.dto.UserPresence;
import com.travelbuddy.model.User;
import com.travelbuddy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OnlineUserService {
    private final Map<String, UserPresence> onlineUsers = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    public void userConnected(String username) {
        User user = userService.findByUsername(username).orElseThrow();
        UserPresence presence = new UserPresence(
                user.getId(),
                username,
                user.getProfilePicture(),
                "ONLINE",
                LocalDateTime.now()
        );
        onlineUsers.put(username, presence);
        log.info("User connected: {}", username);
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
        log.info("User disconnected: {}", username);
    }

    public List<UserPresence> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.values());
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }
}