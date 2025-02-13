package com.travelbuddy.repository;

import com.travelbuddy.model.TripJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripJoinRequestRepository extends JpaRepository<TripJoinRequest, Long> {
    Optional<TripJoinRequest> findByUserIdAndTripId(Long userId, Long tripId);
    List<TripJoinRequest> findByTripId(Long tripId);
}