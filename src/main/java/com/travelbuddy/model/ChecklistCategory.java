package com.travelbuddy.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}