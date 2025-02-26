package com.travelbuddy.repository;

import java.util.List;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE m.tripId = :tripId AND m.id < :messageId " +
            "ORDER BY m.timestamp DESC")
    <T extends MessageContent> List<ChatMessage<T>> findByTripIdAndIdLessThanOrderByTimestampDesc(
            @Param("tripId") Long tripId,
            @Param("messageId") Long messageId,
            Pageable pageable
    );

    @Query("SELECT m FROM ChatMessage m WHERE m.tripId = :tripId " +
            "ORDER BY m.timestamp DESC")
    <T extends MessageContent> List<ChatMessage<T>> findByTripIdOrderByTimestampDesc(
            @Param("tripId") Long tripId,
            Pageable pageable
    );

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user1 AND m.recipient = :user2) OR " +
            "(m.sender = :user2 AND m.recipient = :user1) " +
            "ORDER BY m.timestamp DESC")
    <T extends MessageContent> List<ChatMessage<T>> findPrivateMessages(
            @Param("user1") String user1,
            @Param("user2") String user2,
            Pageable pageable
    );

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "((m.sender = :user1 AND m.recipient = :user2) OR " +
            "(m.sender = :user2 AND m.recipient = :user1)) " +
            "AND m.id < :messageId ORDER BY m.timestamp DESC")
    <T extends MessageContent> List<ChatMessage<T>> findPrivateMessagesBeforeId(
            @Param("user1") String user1,
            @Param("user2") String user2,
            @Param("messageId") Long messageId,
            Pageable pageable
    );
}
