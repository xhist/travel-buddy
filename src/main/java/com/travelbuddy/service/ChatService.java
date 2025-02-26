package com.travelbuddy.service;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.chat.MessageHandler;
import com.travelbuddy.chat.MessageType;
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
    private List<MessageHandler<? extends MessageContent>> messageHandlers;

    private Map<MessageType, MessageHandler<? extends MessageContent>> handlerMap;

    @PostConstruct
    public void init() {
        handlerMap = messageHandlers.stream()
                .collect(Collectors.toMap(
                        MessageHandler::getType,
                        Function.identity()
                ));
    }

    @Override
    public <T extends MessageContent> List<ChatMessage<T>> getMessagesByTripId(Long tripId, Long before, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (before != null) {
            return chatMessageRepository.findByTripIdAndIdLessThanOrderByTimestampDesc(
                    tripId, before, pageRequest);
        }
        return chatMessageRepository.findByTripIdOrderByTimestampDesc(tripId, pageRequest);
    }

    @Override
    public <T extends MessageContent> List<ChatMessage<T>> getPrivateMessages(String user1, String user2, Long before, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (before != null) {
            return chatMessageRepository.findPrivateMessagesBeforeId(
                    user1, user2, before, pageRequest);
        }
        return chatMessageRepository.findPrivateMessages(user1, user2, pageRequest);
    }

    @Override
    public <T extends MessageContent> ChatMessage<T> addReaction(Long messageId, Long userId, String reactionType) {
        ChatMessage<T> message = chatMessageRepository.findById(messageId)
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
    @SuppressWarnings("unchecked")
    public <T extends MessageContent> ChatMessage<T> processMessage(ChatMessage<T> message) {
        final var handler = (MessageHandler<T>) handlerMap.get(message.getType());
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for message type: " + message.getType());
        }

        handler.handle(message);
        return chatMessageRepository.save(message);
    }
}
