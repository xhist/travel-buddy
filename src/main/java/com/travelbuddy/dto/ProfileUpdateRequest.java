package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class ProfileUpdateRequest {
    @Email
    @NotBlank
    private String email;
    private String password;  // Optional: update if provided
    private String profilePicture;
    // New fields for notification preferences:
    private Integer reminderDaysBefore;
    // Expect time in HH:mm format; convert to LocalTime in the controller.
    private String reminderTime;
}
