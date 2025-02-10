package com.travelbuddy.controller;

import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.model.ChatMessageType;
import com.travelbuddy.service.interfaces.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ChatController {

    @Autowired
    private IChatService chatService;

    // Group chat endpoint: broadcast to "/topic/group"
    @PreAuthorize("hasRole('ROLE_USER')")
    @MessageMapping("/group.sendMessage")
    @SendTo("/topic/group")
    public ChatMessage sendGroupMessage(ChatMessage message) {
        message.setType(ChatMessageType.GROUP);
        return chatService.saveMessage(message);
    }

    // Private chat: sent to a specific user’s queue (using send-to-user)
    @PreAuthorize("hasRole('ROLE_USER')")
    @MessageMapping("/private.sendMessage")
    @SendToUser("/queue/private")
    public ChatMessage sendPrivateMessage(ChatMessage message) {
        message.setType(ChatMessageType.PRIVATE);
        return chatService.saveMessage(message);
    }
}
