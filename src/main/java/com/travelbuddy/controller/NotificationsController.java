package com.travelbuddy.controller;

import com.travelbuddy.model.Notification;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.interfaces.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationsController {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getUsername());
        return ResponseEntity.ok(notifications);
    }

    // Method to send notification via WebSocket
    public void sendNotificationToUser(String username, Notification notification) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }
}