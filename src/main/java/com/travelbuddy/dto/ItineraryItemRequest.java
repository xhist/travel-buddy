package com.travelbuddy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItineraryItemRequest {
    @NotBlank
    private String activityName;
    @NotNull
    private Long tripId;
    private Long userId;
}
