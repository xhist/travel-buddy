package com.travelbuddy.chat;

import com.travelbuddy.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class TextMessageHandler implements MessageHandler<TextMessageContent> {
    @Override
    public ChatMessage<TextMessageContent> handle(ChatMessage<TextMessageContent> message) {
        return message;
    }

    @Override
    public MessageType getType() {
        return MessageType.TEXT;
    }
}