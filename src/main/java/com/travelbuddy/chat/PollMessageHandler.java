package com.travelbuddy.chat;

import com.travelbuddy.dto.PollOptionResponse;
import com.travelbuddy.dto.PollRequest;
import com.travelbuddy.model.ChatMessage;
import com.travelbuddy.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PollMessageHandler implements MessageHandler {

    @Autowired
    private final PollService pollService;

    @Override
    public ChatMessage handle(ChatMessage message) {
        if (message.getTripId() == null) {
            throw new IllegalArgumentException("Trip id cannot be null for polling!");
        }

        if (!(message.getContent() instanceof PollMessageContent pollContent)) {
            throw new IllegalArgumentException("Expected PollMessageContent but got " +
                    (message.getContent() != null ? message.getContent().getClass().getSimpleName() : "null"));
        }

        final var pollOptionsNames = pollContent.getOptions().stream()
                .map(PollOptionResponse::getText).toList();
        final var pollRequest = PollRequest.builder()
                .question(pollContent.getQuestion())
                .options(pollOptionsNames)
                .build();
        final var poll = pollService.createPoll(message.getTripId(), pollContent.getSenderId(), pollRequest);
        pollContent.setId(poll.getId());
        pollContent.setOptions(poll.getOptions());
        return message;
    }

    @Override
    public MessageType getType() {
        return MessageType.POLL;
    }
}