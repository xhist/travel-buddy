package com.travelbuddy.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileMessageContent extends MessageContent {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}