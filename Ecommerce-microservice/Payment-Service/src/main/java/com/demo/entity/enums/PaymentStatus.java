package com.demo.entity.enums;

public enum PaymentStatus {
    INITIATED,   // payment order created at Razorpay
    SUCCESS,     // payment verified and successful
    FAILED,      // payment failed
    REFUNDED,
    PENDING// payment refunded

}
