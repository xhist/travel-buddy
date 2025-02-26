package com.travelbuddy.service.interfaces;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.model.ChatMessage;
import java.util.List;

public interface IChatService {
    <T extends MessageContent> ChatMessage<T> processMessage(ChatMessage<T> message);
    <T extends MessageContent> List<ChatMessage<T>> getMessagesByTripId(Long tripId, Long before, int limit);
    <T extends MessageContent> List<ChatMessage<T>> getPrivateMessages(String user1, String user2, Long before, int limit);
    <T extends MessageContent> ChatMessage<T> addReaction(Long messageId, Long userId, String reactionType);
}
