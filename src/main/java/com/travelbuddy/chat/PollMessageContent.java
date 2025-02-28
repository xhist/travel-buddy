package com.travelbuddy.chat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.travelbuddy.dto.PollOptionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("POLL")
public class PollMessageContent extends MessageContent {
    private Long id;
    private String question;
    private List<PollOptionResponse> options;
    private boolean finalized;
}
