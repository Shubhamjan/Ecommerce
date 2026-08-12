package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundStatusDto {
    private String orderNumber;
    private String razorpayPaymentId;
    private String razorpayRefundId;
    private String refundStatus;
    private Object amountRefunded;
    private String speed;
    private LocalDateTime refundedAt;
}
