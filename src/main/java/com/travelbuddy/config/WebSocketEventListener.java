package com.travelbuddy.config;

import com.travelbuddy.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Dedicated component for handling WebSocket events.
 * Using @Lazy to break potential circular dependencies.
 */
@Component
@Slf4j
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    private final ChatRoomService chatRoomService;

    public WebSocketEventListener(@Lazy ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");

        if (username != null) {
            log.debug("User disconnected: {}", username);
            chatRoomService.handleUserDisconnect(username);
        }
    }
}