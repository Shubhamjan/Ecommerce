package com.demo.Notification_Service.dto;

import com.demo.Notification_Service.entity.enums.NotificationStatus;
import com.demo.Notification_Service.entity.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String userId;
    private String userEmail;
    private String subject;
    private String message;
    private String orderNumber;
    private NotificationType notificationType;
    private NotificationStatus notificationStatus;
    private String failureReason;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}