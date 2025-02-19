package com.travelbuddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserPresence {
    private Long id;
    private String username;
    private String profilePicture;
    private String status;
    private LocalDateTime lastSeen;
}