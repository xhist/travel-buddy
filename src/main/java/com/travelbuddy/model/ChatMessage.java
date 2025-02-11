package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String recipient;
    private Long tripId;
    private String content;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private ChatMessageType type;
}
