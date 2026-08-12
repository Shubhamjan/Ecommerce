package com.demo.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private String orderNumber;
    private String userId;
    private String userEmail;
    private String failureReason;
    private double amount;
}