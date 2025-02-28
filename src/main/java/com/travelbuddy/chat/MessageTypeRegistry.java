package com.travelbuddy.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that maps MessageType to its corresponding MessageHandler.
 * This creates a more maintainable approach than hardcoding handlers.
 */
@Component
public class MessageTypeRegistry {

    private final Map<MessageType, MessageHandler> handlerMap = new HashMap<>();

    private final List<MessageHandler> messageHandlers;

    @Autowired
    public MessageTypeRegistry(List<MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
    }

    @PostConstruct
    public void initialize() {
        messageHandlers.forEach(handler ->
                handlerMap.put(handler.getType(), handler)
        );
    }

    /**
     * Get the handler for a specific message type
     */
    public MessageHandler getHandler(MessageType type) {
        MessageHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for message type: " + type);
        }
        return handler;
    }
}