package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class MessageContent {
    private String rawContent;
    private Long senderId;
    private String senderUsername;
}