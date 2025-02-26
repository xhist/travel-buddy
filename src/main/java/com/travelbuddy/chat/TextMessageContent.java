package com.travelbuddy.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextMessageContent extends MessageContent {
    private String text;
}