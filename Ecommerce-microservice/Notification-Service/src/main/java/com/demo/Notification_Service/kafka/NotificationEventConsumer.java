package com.demo.Notification_Service.kafka;

import com.demo.Notification_Service.event.OrderPlacedEvent;
import com.demo.Notification_Service.event.OrderStatusChangedEvent;
import com.demo.Notification_Service.event.PaymentFailedEvent;
import com.demo.Notification_Service.event.PaymentSuccessEvent;
import com.demo.Notification_Service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    // Listen: order placed
    @KafkaListener(
            topics = "order-placed",
            groupId = "notification-group",
            containerFactory = "orderPlacedKafkaListenerFactory"
    )
    public void handleOrderPlaced(@Payload OrderPlacedEvent event){
        log.info("Received order-placed event: {}", event.getOrderNumber());
        notificationService.sendOrderPlacedNotification(event);
    }

    // Listen: payment success
    @KafkaListener(
            topics = "payment-success",
            groupId = "notification-group",
            containerFactory = "paymentSuccessKafkaListenerFactory"
    )
    public void handlePaymentSuccess(@Payload PaymentSuccessEvent event) {
        log.info("Received payment-success event: {}", event.getOrderNumber());
        notificationService.sendPaymentSuccessNotification(event);
    }

    // Listen: payment failed
    @KafkaListener(
            topics = "payment-failed",
            groupId = "notification-group",
            containerFactory = "paymentFailedKafkaListenerFactory"
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("Received payment-failed event: {}", event.getOrderNumber());
        notificationService.sendPaymentFailedNotification(event);
    }


    // Listen: order status changed
    @KafkaListener(
            topics = "order-status-changed",
            groupId = "notification-group",
            containerFactory = "orderStatusChangedKafkaListenerFactory"
    )
    public void handleOrderStatusChanged(
            @Payload OrderStatusChangedEvent event) {
        log.info("Received order-status-changed event: {} status: {}",
                event.getOrderNumber(), event.getOrderStatus());
        notificationService.sendOrderStatusChangedNotification(event);
    }
}
