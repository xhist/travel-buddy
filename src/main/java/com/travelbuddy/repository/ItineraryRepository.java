package com.travelbuddy.repository;

import com.travelbuddy.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<ItineraryItem, Long> {
    @Query(value = "SELECT it FROM ItineraryItem it WHERE it.trip.id = ?1 AND it.user IS NULL")
    List<ItineraryItem> findByTripId(Long tripId);
    List<ItineraryItem> findByTripIdAndUserId(Long tripId, Long userId);
}
