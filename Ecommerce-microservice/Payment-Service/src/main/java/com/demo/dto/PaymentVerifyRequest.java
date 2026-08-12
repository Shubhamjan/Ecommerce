package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {

    @NotBlank(message = "Razorpay order id is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;

    @NotBlank(message = "Order number is required")
    private String orderNumber;
}