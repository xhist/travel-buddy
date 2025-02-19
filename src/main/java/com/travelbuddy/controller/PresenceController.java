package com.travelbuddy.controller;

import com.travelbuddy.model.User;
import com.travelbuddy.service.OnlineUserService;
import com.travelbuddy.service.UserService;
import com.travelbuddy.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class PresenceController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private IUserService userService;

    @MessageMapping("/presence.connect")
    public void connect(Principal principal) {
        String username = principal.getName();
        onlineUserService.userConnected(username);
        broadcastUserPresence(username, "ONLINE");
    }

    @MessageMapping("/presence.disconnect")
    public void disconnect(Principal principal) {
        String username = principal.getName();
        onlineUserService.userDisconnected(username);
        broadcastUserPresence(username, "OFFLINE");
    }

    @MessageMapping("/presence.getOnlineUsers")
    public void getOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/presence", onlineUserService.getOnlineUsers());
    }

    private void broadcastUserPresence(String username, String status) {
        final var user = userService.findByUsername(username);
        Map<String, Object> presenceMessage = new HashMap<>();
        presenceMessage.put("id", user.getId());
        presenceMessage.put("username", username);
        presenceMessage.put("status", status);
        presenceMessage.put("profilePicture", user.getProfilePicture());

        messagingTemplate.convertAndSend("/topic/presence", presenceMessage);
    }
}
