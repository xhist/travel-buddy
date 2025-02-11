package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.ChatMessage;
import java.util.List;

public interface IChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesByTripId(Long tripId);
}
