package com.travelbuddy.service;

import com.travelbuddy.model.Notification;
import com.travelbuddy.model.NotificationType;
import com.travelbuddy.repository.NotificationRepository;
import com.travelbuddy.service.interfaces.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class NotificationService implements INotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotification(String username, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .username(username)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        // Save to database
        notification = notificationRepository.save(notification);

        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );

        log.info("Sent notification to user {}: {}", username, title);
    }

    @Override
    public List<Notification> getNotificationsForUser(String username) {
        return notificationRepository.findByUsernameOrderByTimestampDesc(username);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}