package com.travelbuddy.chat;

import com.travelbuddy.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class TextMessageHandler implements MessageHandler {
    @Override
    public ChatMessage handle(ChatMessage message) {
        // Validate the content type
        if (!(message.getContent() instanceof TextMessageContent)) {
            throw new IllegalArgumentException("Expected TextMessageContent but got " +
                    (message.getContent() != null ? message.getContent().getClass().getSimpleName() : "null"));
        }
        return message;
    }

    @Override
    public MessageType getType() {
        return MessageType.TEXT;
    }
}