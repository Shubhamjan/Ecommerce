package com.demo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundCompletedEvent {

    private String orderNumber;
    private String razorpayRefundId;
    private String refundId;
    private double amount;
    private String userEmail;
    private LocalDateTime refundTime;
}
