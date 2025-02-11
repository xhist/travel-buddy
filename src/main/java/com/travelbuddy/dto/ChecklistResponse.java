package com.travelbuddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ChecklistResponse {
    private Long id;
    private String name;
    private String category;
    private Long tripId;
    private Long userId;
}
