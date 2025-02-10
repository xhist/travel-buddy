package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferences {
    private Integer reminderDaysBefore;
    private LocalTime reminderTime;
}
