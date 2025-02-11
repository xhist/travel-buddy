package com.travelbuddy.repository;

import com.travelbuddy.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByTimestampDesc(String username);
    List<Notification> findByUsernameAndIsReadOrderByTimestampDesc(String username, boolean isRead);
}