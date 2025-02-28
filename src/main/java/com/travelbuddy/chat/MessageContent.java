package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessageContent.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ImageMessageContent.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = FileMessageContent.class, name = "FILE"),
        @JsonSubTypes.Type(value = PollMessageContent.class, name = "POLL")
})
public abstract class MessageContent {
    private Long senderId;
    private String senderUsername;
}