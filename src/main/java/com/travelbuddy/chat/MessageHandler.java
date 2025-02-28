package com.travelbuddy.chat;

import com.travelbuddy.model.ChatMessage;

public interface MessageHandler {
    ChatMessage handle(ChatMessage message);
    MessageType getType();
}