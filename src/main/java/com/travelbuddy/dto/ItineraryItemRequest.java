package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ItineraryItemRequest {
    @NotBlank
    private String activityName;
    @NotNull
    private LocalDateTime activityDateTime;
    @NotBlank
    private String location;
    private String notes;
    @NotNull
    private Long tripId;
}
