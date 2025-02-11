package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class ProfileUpdateRequest {
    @Email
    @NotBlank
    private String email;
    private String profilePicture;
    private String bio;
    private Integer reminderDaysBefore;
    private String reminderTime;
}
