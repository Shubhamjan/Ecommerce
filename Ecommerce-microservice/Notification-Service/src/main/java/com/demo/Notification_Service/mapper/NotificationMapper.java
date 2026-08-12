package com.demo.Notification_Service.mapper;

import com.demo.Notification_Service.dto.NotificationDto;
import com.demo.Notification_Service.entity.Notification;

public class NotificationMapper {

    public NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .userEmail(notification.getUserEmail())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .orderNumber(notification.getOrderNumber())
                .notificationType(notification.getNotificationType())
                .notificationStatus(notification.getNotificationStatus())
                .failureReason(notification.getFailureReason())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
