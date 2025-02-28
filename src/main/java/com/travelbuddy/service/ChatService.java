package com.travelbuddy.service;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.chat.MessageHandler;
import com.travelbuddy.chat.MessageType;
import com.travelbuddy.chat.MessageTypeRegistry;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.model.MessageReaction;
import com.travelbuddy.repository.ChatMessageRepository;
import com.travelbuddy.service.interfaces.IChatService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService implements IChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MessageTypeRegistry messageTypeRegistry;

    @Override
    public List<ChatMessage> getMessagesByTripId(Long tripId, Long before, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (before != null) {
            return chatMessageRepository.findByTripIdAndIdLessThanOrderByTimestampDesc(
                    tripId, before, pageRequest);
        }
        return chatMessageRepository.findByTripIdOrderByTimestampDesc(tripId, pageRequest);
    }

    @Override
    public List<ChatMessage> getPrivateMessages(String user1, String user2, Long before, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (before != null) {
            return chatMessageRepository.findPrivateMessagesBeforeId(
                    user1, user2, before, pageRequest);
        }
        return chatMessageRepository.findPrivateMessages(user1, user2, pageRequest);
    }

    @Override
    public ChatMessage addReaction(Long messageId, String username, String reactionType) {
        final var message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Remove existing reaction from this user if exists
        message.getReactions().removeIf(reaction -> reaction.getUsername().equals(username));

        // Add new reaction
        MessageReaction reaction = MessageReaction.builder()
                .username(username)
                .reactionType(reactionType)
                .timestamp(LocalDateTime.now())
                .build();

        message.getReactions().add(reaction);
        return chatMessageRepository.save(message);
    }

    @Override
    public ChatMessage processMessage(ChatMessage message) {
        final var handler = messageTypeRegistry.getHandler(message.getType());
        handler.handle(message);
        return chatMessageRepository.save(message);
    }

    @Override
    public List<Map<String, Object>> getUnreadMessageCounts(String username) {
        // Find all unread messages where current user is the recipient
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesForUser(username);

        // Group by sender
        Map<String, Long> countBySender = unreadMessages.stream()
                .collect(Collectors.groupingBy(ChatMessage::getSender, Collectors.counting()));

        // Convert to the required format for the response
        List<Map<String, Object>> result = new ArrayList<>();
        countBySender.forEach((sender, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("sender", sender);
            item.put("count", count);
            result.add(item);
        });

        return result;
    }

    @Override
    public void markMessagesAsRead(String sender, String recipient) {
        chatMessageRepository.markMessagesAsRead(sender, recipient);
    }
}