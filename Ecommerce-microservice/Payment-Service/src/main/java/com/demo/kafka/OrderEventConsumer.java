package com.demo.kafka;

import com.demo.entity.Payment;
import com.demo.entity.enums.PaymentStatus;
import com.demo.event.OrderPlacedEvent;
import com.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentRepository paymentRepository;

    // Listen to order-placed event
    // Create a pending payment record automatically
    @KafkaListener(
        topics = "order-placed",
        groupId = "payment-group"
    )
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order-placed event for: {}", event.getOrderNumber());

        // check if payment already exists for this order
        boolean exists = paymentRepository
            .findByOrderNumber(event.getOrderNumber()).isPresent();

        if (!exists) {
            Payment payment = Payment.builder()
                .orderNumber(event.getOrderNumber())
                .userId(event.getUserId())
                .userEmail(event.getUserEmail())
                .amount(event.getFinalAmount())
                .currency("INR")
                .paymentStatus(PaymentStatus.INITIATED)
                .build();

            paymentRepository.save(payment);
            log.info("Payment record created for order: {}",
                event.getOrderNumber());
        }
    }
}