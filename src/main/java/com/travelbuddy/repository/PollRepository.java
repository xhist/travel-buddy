package com.travelbuddy.repository;

import com.travelbuddy.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByTripIdOrderByCreatedAtDesc(Long tripId);
}