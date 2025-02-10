package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.ChatMessage;

public interface IChatService {
    ChatMessage saveMessage(ChatMessage message);
}
