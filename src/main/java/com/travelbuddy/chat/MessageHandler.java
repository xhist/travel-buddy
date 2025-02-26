package com.travelbuddy.chat;

import com.travelbuddy.model.ChatMessage;

public interface MessageHandler<T extends MessageContent> {
    ChatMessage<T> handle(ChatMessage<T> message);
    MessageType getType();
}