package com.travelbuddy.repository;

import java.util.List;
import com.travelbuddy.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTripId(Long tripId);
}
