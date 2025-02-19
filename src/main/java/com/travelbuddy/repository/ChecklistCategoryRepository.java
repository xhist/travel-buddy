package com.travelbuddy.repository;

import com.travelbuddy.model.ChecklistCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChecklistCategoryRepository extends JpaRepository<ChecklistCategory, Long> {
    Optional<ChecklistCategory> findByName(String name);
}