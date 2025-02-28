package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IMAGE")
public class ImageMessageContent extends MessageContent {
    private String imageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
}