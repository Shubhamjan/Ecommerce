package com.demo.mapper;

import com.demo.dto.PaymentDto;
import com.demo.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDto mapToPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
