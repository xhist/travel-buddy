package com.travelbuddy.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generic converter for MessageContent objects that leverages Jackson's
 * polymorphic type handling to automatically determine the correct subclass.
 */
@Converter(autoApply = false)
@Component
public class MessageContentConverter implements AttributeConverter<MessageContent, String> {

    private static final Logger log = LoggerFactory.getLogger(MessageContentConverter.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public MessageContentConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(MessageContent content) {
        if (content == null) {
            return null;
        }

        try {
            // Jackson will include type information because of the @JsonTypeInfo annotation
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("Error converting MessageContent to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting message content to JSON", e);
        }
    }

    @Override
    public MessageContent convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            // Jackson will create the appropriate subclass based on type information
            return objectMapper.readValue(dbData, MessageContent.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to MessageContent: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting JSON to message content", e);
        }
    }
}