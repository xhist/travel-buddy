package com.travelbuddy.controller;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.chat.MessageType;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.security.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    @Autowired
    private IChatService chatService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/messages/trip/{tripId}")
    public ResponseEntity<List<ChatMessage>> getTripMessages(
            @PathVariable Long tripId,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessage> messages = chatService.getMessagesByTripId(tripId, before, limit);

        // Ensure content is available in a predictable format for the frontend
        messages.forEach(message -> {
            if (message.getContent() != null) {
                // Add type information to the message to help the frontend
                if (message.getType() == null) {
                    message.setType(MessageType.valueOf(message.getContent().getClass().getSimpleName()));
                }
            }
        });

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<ChatMessage> addReaction(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ChatMessage message = chatService.addReaction(messageId, currentUser.getUsername(), request.get("reactionType"));
        return ResponseEntity.ok(message);
    }

    @GetMapping("/messages/private/{username}")
    public ResponseEntity<List<ChatMessage>> getPrivateMessages(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessage> messages = chatService.getPrivateMessages(
                currentUser.getUsername(),
                username,
                before,
                limit
        );
        return ResponseEntity.ok(messages);
    }

    /**
     * Get unread messages count for the current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadMessages(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(chatService.getUnreadMessageCounts(currentUser.getUsername()));
    }

    /**
     * Mark messages from a specific sender as read
     */
    @PostMapping("/markAsRead/{sender}")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @PathVariable String sender,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        chatService.markMessagesAsRead(sender, currentUser.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @MessageMapping("/chat.trip.{tripId}")
    @SendTo("/topic/trip/{tripId}")
    public ChatMessage sendTripMessage(@Payload ChatMessage message,
                                       @DestinationVariable Long tripId,
                                       Principal principal) {
        message.setSender(principal.getName());
        message.setTripId(tripId);
        message.setTimestamp(LocalDateTime.now());
        return chatService.processMessage(message);
    }

    @MessageMapping("/chat.private")
    public ChatMessage sendPrivateMessage(@Payload ChatMessage message,
                                          Principal principal,
                                          SimpMessageHeaderAccessor headerAccessor) {
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());
        ChatMessage savedMessage = chatService.processMessage(message);

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

    @MessageMapping("/chat.typing")
    public void updateTypingStatus(Principal principal, @Payload Map<String, Object> payload) {
        String recipient = (String) payload.get("recipient");
        boolean typing = (boolean) payload.get("typing");

        Map<String, Object> typingStatus = new HashMap<>();
        typingStatus.put("sender", principal.getName());
        typingStatus.put("typing", typing);

        simpMessagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/typing",
                typingStatus
        );
    }
}