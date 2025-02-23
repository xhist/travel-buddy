package com.travelbuddy.service;

import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.model.MessageReaction;
import com.travelbuddy.repository.ChatMessageRepository;
import com.travelbuddy.service.interfaces.IChatService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatService implements IChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

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
    public ChatMessage addReaction(Long messageId, Long userId, String reactionType) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Remove existing reaction from this user if exists
        message.getReactions().removeIf(reaction -> reaction.getUserId().equals(userId));

        // Add new reaction
        MessageReaction reaction = MessageReaction.builder()
                .userId(userId)
                .reactionType(reactionType)
                .timestamp(LocalDateTime.now())
                .build();

        message.getReactions().add(reaction);
        return chatMessageRepository.save(message);
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }
}
