package com.travelbuddy.controller;

import com.travelbuddy.model.UserPresenceStatus;
import com.travelbuddy.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Set;

@Controller
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * Handle explicit room join requests
     */
    @MessageMapping("/room.{roomId}.join")
    public void joinRoom(@DestinationVariable Long roomId, Principal principal) {
        chatRoomService.addUserToRoom(roomId, principal.getName());
    }

    /**
     * Handle explicit room leave requests
     */
    @MessageMapping("/room.{roomId}.leave")
    public void leaveRoom(@DestinationVariable Long roomId, Principal principal) {
        chatRoomService.removeUserFromRoom(roomId, principal.getName());
    }

    /**
     * Handle typing status updates
     */
    @MessageMapping("/room.{roomId}.typing")
    public void updateTypingStatus(
            @DestinationVariable Long roomId,
            Principal principal,
            boolean typing) {
        chatRoomService.updateUserTypingStatus(roomId, principal.getName(), typing);
    }

    /**
     * Respond with active users when client subscribes to the user presence topic
     */
    @SubscribeMapping("/room/{roomId}/users")
    public Set<UserPresenceStatus> getActiveUsers(@DestinationVariable Long roomId) {
        return chatRoomService.getUserStatuses(roomId);
    }
}