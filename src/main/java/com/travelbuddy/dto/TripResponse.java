package com.travelbuddy.dto;

import com.travelbuddy.model.TripStatus;
import com.travelbuddy.model.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TripResponse {
    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private TripStatus status;
    private Long organizer;
    private List<UserDto> members;
}
