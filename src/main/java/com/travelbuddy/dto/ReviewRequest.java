package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReviewRequest {
    @NotBlank
    private String reviewer;
    @NotBlank
    private String reviewee;
    @NotBlank
    private String comment;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
