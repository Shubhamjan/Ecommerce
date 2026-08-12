package com.demo.Notification_Service.service.impl;

import com.demo.Notification_Service.dto.NotificationDto;
import com.demo.Notification_Service.entity.Notification;
import com.demo.Notification_Service.entity.enums.NotificationStatus;
import com.demo.Notification_Service.entity.enums.NotificationType;
import com.demo.Notification_Service.event.OrderPlacedEvent;
import com.demo.Notification_Service.event.OrderStatusChangedEvent;
import com.demo.Notification_Service.event.PaymentFailedEvent;
import com.demo.Notification_Service.event.PaymentSuccessEvent;
import com.demo.Notification_Service.exception.NotificationNotFoundException;
import com.demo.Notification_Service.mapper.NotificationMapper;
import com.demo.Notification_Service.repository.NotificationRepository;
import com.demo.Notification_Service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ValueRange;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private NotificationMapper mapper;

    @Override
    public void sendOrderPlacedNotification(OrderPlacedEvent event) {

        String subject = "Order Placed Successfully - "+event.getOrderNumber();

        Map<String,Object> variables = new HashMap<>();
        variables.put("orderNumber",event.getOrderNumber());
        variables.put("userEmail",event.getUserEmail());
        variables.put("finalAmount", event.getFinalAmount());
        variables.put("items", event.getItems());

        sendAndSave(
                event.getUserId(),
                event.getUserEmail(),
                subject,
                event.getOrderNumber(),
                NotificationType.ORDER_PLACED,
                "order-placed",
                variables
        );
    }

    @Override
    public void sendPaymentSuccessNotification(PaymentSuccessEvent event) {

        String subject = "Payment Successful - " + event.getOrderNumber();

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", event.getOrderNumber());
        variables.put("userEmail", event.getUserEmail());
        variables.put("amount", event.getAmount());
        variables.put("razorpayPaymentId", event.getRazorpayPaymentId());

        sendAndSave(
                event.getUserId(),
                event.getUserEmail(),
                subject,
                event.getOrderNumber(),
                NotificationType.PAYMENT_SUCCESS,
                "payment-success",
                variables
        );
    }

    @Override
    public void sendPaymentFailedNotification(PaymentFailedEvent event) {

        String subject = "Payment Failed - " + event.getOrderNumber();

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", event.getOrderNumber());
        variables.put("userEmail", event.getUserEmail());
        variables.put("amount", event.getAmount());
        variables.put("failureReason", event.getFailureReason());

        sendAndSave(
                event.getUserId(),
                event.getUserEmail(),
                subject,
                event.getOrderNumber(),
                NotificationType.PAYMENT_FAILED,
                "payment-failed",
                variables
        );
    }

    @Override
    public void sendOrderStatusChangedNotification(OrderStatusChangedEvent event) {


        // determine notification type and subject based on status
        NotificationType type;
        String subject;

        switch (event.getOrderStatus()) {
            case "CONFIRMED" -> {
                type = NotificationType.ORDER_CONFIRMED;
                subject = "Order Confirmed - " + event.getOrderNumber();
            }
            case "PROCESSING" -> {
                type = NotificationType.ORDER_PROCESSING;
                subject = "Order Being Prepared - " + event.getOrderNumber();
            }
            case "READY" -> {
                type = NotificationType.ORDER_READY;
                subject = "Your Order is Ready for Pickup! - "
                        + event.getOrderNumber();
            }
            case "COMPLETED" -> {
                type = NotificationType.ORDER_COMPLETED;
                subject = "Order Completed - " + event.getOrderNumber();
            }
            case "CANCELLED" -> {
                type = NotificationType.ORDER_CANCELLED;
                subject = "Order Cancelled - " + event.getOrderNumber();
            }
            default -> {
                type = NotificationType.ORDER_CONFIRMED;
                subject = "Order Update - " + event.getOrderNumber();
            }
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", event.getOrderNumber());
        variables.put("userEmail", event.getUserEmail());
        variables.put("orderStatus", event.getOrderStatus());
        variables.put("pickupStatus", event.getPickupStatus());

        sendAndSave(
                event.getUserId(),
                event.getUserEmail(),
                subject,
                event.getOrderNumber(),
                type,
                "order-status-changed",
                variables
        );
    }

    @Override
    public Page<NotificationDto> getMyNotifications(String userId, Pageable pageable) {


        return notificationRepository.findAll(pageable).map(mapper::mapToDto);
    }

    @Override
    public Page<NotificationDto> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(mapper::mapToDto);
    }

    @Override
    public NotificationDto getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .map(mapper::mapToDto)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + id));
    }


    // ── Helper — Send Email & Save to DB ──────────────────

    private void sendAndSave(String userId, String userEmail, String subject, String orderNumber,
                             NotificationType type, String templateName, Map<String,Object> variables){

        Notification notification = Notification.builder()
                .userId(userId)
                .userEmail(userEmail)
                .subject(subject)
                .orderNumber(orderNumber)
                .notificationType(type)
                .notificationStatus(NotificationStatus.PENDING)
                .build();
        try{
            emailService.sendHtmlEmail(userEmail,subject,templateName,variables);
            // mark as sent
            notification.setNotificationStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setMessage("Email sent successfully");

            log.info("Notification sent: {} to {}", type, userEmail);

        }catch(Exception e){
            notification.setNotificationStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());

            log.error("Failed to send notification: {} to {} reason: {}",
                    type, userEmail, e.getMessage());
        }
        notificationRepository.save(notification);
    }
}


