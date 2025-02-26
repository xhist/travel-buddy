package com.travelbuddy.chat;

import com.travelbuddy.model.PollOption;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PollMessageContent extends MessageContent {
    private String question;
    private List<String> options;
    private boolean finalized;
}
