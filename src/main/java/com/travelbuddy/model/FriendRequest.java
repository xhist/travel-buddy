package com.travelbuddy.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "friend_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // The user who sent the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;
    // The user who receives the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;
    private boolean accepted;
}
