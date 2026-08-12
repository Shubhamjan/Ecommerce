package com.demo.kafka;

import com.demo.event.PaymentFailedEvent;
import com.demo.event.PaymentSuccessEvent;
import com.demo.event.RefundCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send("payment-success", event);
        log.info("Payment success event published for order: {}",
            event.getOrderNumber());
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send("payment-failed", event);
        log.info("Payment failed event published for order: {}",
            event.getOrderNumber());
    }

    public void publishRefundEvent(RefundCompletedEvent refundEvent) {

        kafkaTemplate.send("refund-done",refundEvent);
        log.info("The refund event publish with refund id :- ",refundEvent.getRefundId());
    }
}