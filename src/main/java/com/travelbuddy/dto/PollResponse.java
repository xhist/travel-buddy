package com.travelbuddy.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {
    private Long id;
    private String question;
    private UserDto creator;
    private List<PollOptionResponse> options;
    private LocalDateTime createdAt;
    private Boolean finalized;
}