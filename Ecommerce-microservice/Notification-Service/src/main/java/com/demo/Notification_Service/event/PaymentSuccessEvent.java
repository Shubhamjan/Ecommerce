package com.demo.Notification_Service.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {
    private String orderNumber;
    private String userId;
    private String userEmail;
    private String razorpayPaymentId;
    private double amount;
}