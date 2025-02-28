package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.ChatMessage;
import java.util.List;
import java.util.Map;

public interface IChatService {
    ChatMessage processMessage(ChatMessage message);
    List<ChatMessage> getMessagesByTripId(Long tripId, Long before, int limit);
    List<ChatMessage> getPrivateMessages(String user1, String user2, Long before, int limit);
    ChatMessage addReaction(Long messageId, String username, String reactionType);
    List<Map<String, Object>> getUnreadMessageCounts(String username);
    void markMessagesAsRead(String sender, String recipient);
}