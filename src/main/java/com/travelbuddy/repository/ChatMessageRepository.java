package com.travelbuddy.repository;

import java.util.List;
import com.travelbuddy.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTripId(Long tripId);
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "((m.sender = :sender AND m.recipient = :recipient) OR " +
            "(m.sender = :recipient AND m.recipient = :sender)) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findPrivateMessages(
            @Param("sender") String sender,
            @Param("recipient") String recipient
    );
}
