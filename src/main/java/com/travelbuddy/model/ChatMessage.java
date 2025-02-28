package com.travelbuddy.model;

import com.travelbuddy.chat.MessageContent;
import com.travelbuddy.chat.MessageContentConverter;
import com.travelbuddy.chat.MessageType;
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

    @Enumerated(EnumType.STRING)
    private MessageType type;

    /**
     * The message content stored as JSON in the database.
     * Using a custom converter to handle polymorphic content types.
     */
    @Convert(converter = MessageContentConverter.class)
    @Column(name = "content", columnDefinition = "TEXT")
    private MessageContent content;

    private LocalDateTime timestamp;

    @Builder.Default
    private boolean isRead = false;

    @ElementCollection
    @CollectionTable(name = "message_reactions",
            joinColumns = @JoinColumn(name = "message_id"))
    private List<MessageReaction> reactions = new ArrayList<>();
}