package com.travelbuddy.chat;

import com.travelbuddy.dto.PollRequest;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PollMessageHandler implements MessageHandler<PollMessageContent> {

    @Autowired
    private final PollService pollService;

    @Override
    public ChatMessage<PollMessageContent> handle(ChatMessage<PollMessageContent> message) {
        if (message.getTripId() == null) {
            throw new IllegalArgumentException("Trip id cannot be null for polling!");
        }
        final var pollContent = message.getContent();
        final var pollRequest = PollRequest.builder()
                .question(pollContent.getQuestion())
                .options(pollContent.getOptions())
                .build();
        pollService.createPoll(message.getTripId(), message.getContent().getSenderId(), pollRequest);
        return message;
    }

    @Override
    public MessageType getType() {
        return MessageType.POLL;
    }
}