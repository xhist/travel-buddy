package com.travelbuddy.repository;

import com.travelbuddy.model.PackingChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PackingChecklistRepository extends JpaRepository<PackingChecklistItem, Long> {
    List<PackingChecklistItem> findByTripId(Long tripId);
}
