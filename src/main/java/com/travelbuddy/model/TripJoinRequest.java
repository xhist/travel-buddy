package com.travelbuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_join_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_trip", columnNames = {"user_id", "trip_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private LocalDateTime requestDate;
}