package com.travelbuddy.repository;

import com.travelbuddy.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByOrganizerId(Long organizerId);
}
