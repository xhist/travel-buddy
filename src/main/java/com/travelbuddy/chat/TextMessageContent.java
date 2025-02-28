package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TEXT")
public class TextMessageContent extends MessageContent {
    private String text;
}