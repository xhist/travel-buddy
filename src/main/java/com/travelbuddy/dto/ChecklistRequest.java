package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ChecklistRequest {
    @NotBlank
    private String itemName;
    @NotBlank
    private String category;
    @NotNull
    private Long tripId;
}
