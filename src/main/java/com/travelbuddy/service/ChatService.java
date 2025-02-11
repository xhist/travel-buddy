package com.travelbuddy.service;

import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.repository.ChatMessageRepository;
import com.travelbuddy.service.interfaces.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ChatService implements IChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Chat message saved from {} to {}", savedMessage.getSender(), savedMessage.getRecipient());
        return savedMessage;
    }

    @Override
    public List<ChatMessage> getMessagesByTripId(Long tripId) {
        return chatMessageRepository.findByTripId(tripId);
    }
}
