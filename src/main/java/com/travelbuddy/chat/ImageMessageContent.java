package com.travelbuddy.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageMessageContent extends MessageContent {
    private String imageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
}