package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("FILE")
public class FileMessageContent extends MessageContent {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}