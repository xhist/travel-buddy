package com.travelbuddy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChecklistRequest {
    @NotBlank
    private String itemName;
    @NotBlank
    private String category;
    @NotNull
    private Long tripId;
    private Long userId;
}
