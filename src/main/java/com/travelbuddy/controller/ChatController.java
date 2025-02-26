package com.travelbuddy.controller;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.interfaces.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/messages/trip/{tripId}")
    public ResponseEntity<List<ChatMessage<MessageContent>>> getTripMessages(
            @PathVariable Long tripId,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessage<MessageContent>> messages = chatService.getMessagesByTripId(tripId, before, limit);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<ChatMessage<MessageContent>> addReaction(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ChatMessage<MessageContent> message = chatService.addReaction(messageId, currentUser.getId(), request.get("reactionType"));
        return ResponseEntity.ok(message);
    }

    @GetMapping("/messages/private/{username}")
    public ResponseEntity<List<ChatMessage<MessageContent>>> getPrivateMessages(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessage<MessageContent>> messages = chatService.getPrivateMessages(
                currentUser.getUsername(),
                username,
                before,
                limit
        );
        return ResponseEntity.ok(messages);
    }

    // This endpoint remains in WebSocketConfig.java but modified to handle reactions
    @MessageMapping("/chat.trip.{tripId}")
    @SendTo("/topic/trip/{tripId}")
    public ChatMessage<MessageContent> sendTripMessage(@Payload ChatMessage<MessageContent> message,
                                       @DestinationVariable Long tripId,
                                       Principal principal) {
        message.setSender(principal.getName());
        message.setTripId(tripId);
        message.setTimestamp(LocalDateTime.now());
        return chatService.processMessage(message);
    }

    @MessageMapping("/chat.private")
    public ChatMessage<MessageContent> sendPrivateMessage(@Payload ChatMessage<MessageContent> message,
                                          Principal principal,
                                          SimpMessageHeaderAccessor headerAccessor) {
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());
        ChatMessage<MessageContent> savedMessage = chatService.processMessage(message);

        // Send to recipient
        simpMessagingTemplate.convertAndSendToUser(
                message.getRecipient(),
                "/queue/private",
                savedMessage
        );

        // Send back to sender
        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/private",
                savedMessage
        );

        return savedMessage;
    }
}