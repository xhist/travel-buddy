package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.ChatMessage;
import java.util.List;

public interface IChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesByTripId(Long tripId, Long before, int limit);
    List<ChatMessage> getPrivateMessages(String user1, String user2, Long before, int limit);
    ChatMessage addReaction(Long messageId, Long userId, String reactionType);
}
