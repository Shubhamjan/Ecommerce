package com.demo.Notification_Service.repository;


import com.demo.Notification_Service.entity.Notification;
import com.demo.Notification_Service.entity.enums.NotificationStatus;
import com.demo.Notification_Service.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(String userId, Pageable pageable);
    Page<Notification> findByUserIdAndNotificationType(
            String userId, NotificationType type, Pageable pageable);
    List<Notification> findByNotificationStatus(NotificationStatus status);
    Page<Notification> findAll(Pageable pageable);
}