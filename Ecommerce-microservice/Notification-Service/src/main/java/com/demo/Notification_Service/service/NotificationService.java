package com.demo.Notification_Service.service;

import com.demo.Notification_Service.dto.NotificationDto;
import com.demo.Notification_Service.event.OrderPlacedEvent;
import com.demo.Notification_Service.event.OrderStatusChangedEvent;
import com.demo.Notification_Service.event.PaymentFailedEvent;
import com.demo.Notification_Service.event.PaymentSuccessEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void sendOrderPlacedNotification(OrderPlacedEvent event);
    void sendPaymentSuccessNotification(PaymentSuccessEvent event);
    void sendPaymentFailedNotification(PaymentFailedEvent event);
    void sendOrderStatusChangedNotification(OrderStatusChangedEvent event);
    Page<NotificationDto> getMyNotifications(String userId, Pageable pageable);
    Page<NotificationDto> getAllNotifications(Pageable pageable);
    NotificationDto getNotificationById(Long id);
}