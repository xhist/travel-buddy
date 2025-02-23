package com.travelbuddy.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    private String profilePicture;
    private String bio;
    @Embedded
    private NotificationPreferences notificationPreferences;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(name = "user_friends",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "friend_id"))
//    @JsonManagedReference("friends")
    private Set<User> friendsReference = new HashSet<>();

    @ManyToMany(mappedBy="friendsReference")
    private Set<User> friends;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private List<Trip> trips;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                "}";
    }
}