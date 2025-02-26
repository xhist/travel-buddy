package com.travelbuddy.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class MessageContentConverter implements AttributeConverter<MessageContent, String> {

    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        MessageContentConverter.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(MessageContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new RuntimeException("Error converting message content to JSON", e);
        }
    }

    @Override
    public MessageContent convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, MessageContent.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to message content", e);
        }
    }
}