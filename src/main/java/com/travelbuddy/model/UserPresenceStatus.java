package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresenceStatus {
    private String username;
    private LocalDateTime lastSeen;
    private boolean typing;
}