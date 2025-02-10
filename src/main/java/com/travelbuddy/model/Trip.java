package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    @Enumerated(EnumType.STRING)
    private TripStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    private User organizer;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trip_members",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> members;
    // Users who requested to join and are pending approval
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trip_join_requests",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> joinRequests;
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackingChecklistItem> packingChecklistItems;
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryItem> itineraryItems;
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses;
}
