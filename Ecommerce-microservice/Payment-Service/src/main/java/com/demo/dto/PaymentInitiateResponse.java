package com.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateResponse {
    private String razorpayOrderId;  // send to frontend
    private String orderNumber;
    private double amount;
    private String currency;
    private String keyId;            // Razorpay key id for frontend
}