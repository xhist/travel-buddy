package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String fileName;  // For file messages

    @ElementCollection
    @CollectionTable(name = "message_reactions",
            joinColumns = @JoinColumn(name = "message_id"))
    private List<MessageReaction> reactions = new ArrayList<>();
}
