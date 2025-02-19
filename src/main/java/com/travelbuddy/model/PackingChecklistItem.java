package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "packing_checklist_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    @ManyToOne
    @JoinColumn(name = "checklist_categories_id")
    private ChecklistCategory category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
