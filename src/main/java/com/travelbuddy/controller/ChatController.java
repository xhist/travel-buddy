package com.travelbuddy.controller;

import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.model.ChatMessageType;
import com.travelbuddy.service.interfaces.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ChatController {

    @Autowired
    private IChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PreAuthorize("hasRole('ROLE_USER')")
    @MessageMapping("/chat.trip.{tripId}")
    public ChatMessage sendTripMessage(@Payload ChatMessage message, @DestinationVariable String tripId, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        message.setType(ChatMessageType.GROUP);
        message.setSender(username);
        ChatMessage savedMessage = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/trip/" + tripId, savedMessage);
        return savedMessage;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @MessageMapping("/private.sendMessage")
    public ChatMessage sendPrivateMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        message.setType(ChatMessageType.PRIVATE);
        message.setSender(username);

        ChatMessage savedMessage = chatService.saveMessage(message);

        // Send to recipient
        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),
                "/queue/private",
                savedMessage
        );

        // Send back to sender
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/private",
                savedMessage
        );

        return savedMessage;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @MessageMapping("/notification")
    public void sendNotification(@Payload String notificationMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notificationMessage
        );
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/api/chat/trip/{tripId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatMessagesForTrip(@PathVariable Long tripId) {
        List<ChatMessage> messages = chatService.getMessagesByTripId(tripId);
        return ResponseEntity.ok(messages);
    }
}