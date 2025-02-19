package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class TripRequest {
    @NotBlank
    private Long id;
    @NotBlank
    private String title;
    @NotBlank
    private String destination;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotBlank
    private String description;
}
