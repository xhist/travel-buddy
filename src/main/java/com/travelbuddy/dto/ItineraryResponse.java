package com.travelbuddy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItineraryResponse {
    private Long id;
    private String activityName;
    private Long tripId;
    private Long userId;
}
