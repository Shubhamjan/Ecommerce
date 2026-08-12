package com.demo.dto;

import com.demo.entity.enums.PaymentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private String orderNumber;
    private String userId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private double amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}