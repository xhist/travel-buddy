package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.Notification;
import com.travelbuddy.model.NotificationType;

import java.util.List;

public interface INotificationService {
    void sendNotification(String username, String title, String message, NotificationType type);
    List<Notification> getNotificationsForUser(String username);
    void markAsRead(Long notificationId);
}
