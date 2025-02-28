package com.travelbuddy.config;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a user joins a chat room
 */
@Getter
public class RoomJoinEvent extends ApplicationEvent {
    private final String username;
    private final Long roomId;

    public RoomJoinEvent(String username, Long roomId) {
        super(username);
        this.username = username;
        this.roomId = roomId;
    }
}