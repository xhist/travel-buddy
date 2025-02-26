package com.travelbuddy.chat;

import java.util.Arrays;

public enum MessageType {
    TEXT(TextMessageContent.class),
    POLL(PollMessageContent.class),
    IMAGE(ImageMessageContent.class),
    FILE(FileMessageContent.class);

    private final Class<? extends MessageContent> contentClass;

    MessageType(Class<? extends MessageContent> contentClass) {
        this.contentClass = contentClass;
    }

    public Class<? extends MessageContent> getContentClass() {
        return contentClass;
    }

    public static MessageType fromContent(MessageContent content) {
        return Arrays.stream(values())
                .filter(type -> type.getContentClass().equals(content.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown message content type"));
    }
}
