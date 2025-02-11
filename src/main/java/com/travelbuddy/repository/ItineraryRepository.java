package com.travelbuddy.repository;

import com.travelbuddy.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItineraryRepository extends JpaRepository<ItineraryItem, Long> {
    List<ItineraryItem> findByTripId(Long tripId);
    List<ItineraryItem> findByTripIdAndUserId(Long tripId, Long userId);
}
