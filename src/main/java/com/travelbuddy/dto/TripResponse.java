package com.travelbuddy.dto;

import com.travelbuddy.model.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TripResponse {
    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String status;
    private User organizer;
}
