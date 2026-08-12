package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @Positive(message = "Amount must be positive")
    private double amount;

    // default INR
    private String currency = "INR";
}