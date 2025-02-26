package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    private Trip trip;

    @ElementCollection
    @CollectionTable(name = "chat_room_online_users",
            joinColumns = @JoinColumn(name = "room_id"))
    private Set<String> onlineUsers = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "chat_room_user_status",
            joinColumns = @JoinColumn(name = "room_id"))
    private Set<UserPresenceStatus> userStatuses = new HashSet<>();
}