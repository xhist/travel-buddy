package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "itinerary_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String activityName;
    private LocalDateTime activityDateTime;
    private String location;
    private String notes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;
}
