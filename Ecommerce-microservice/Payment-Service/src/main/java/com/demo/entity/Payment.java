package com.demo.entity;

import com.demo.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // our order number
    @Column(nullable = false)
    private String orderNumber;

    // user who is paying
    @Column(nullable = false)
    private String userId;

    private String userEmail;

    // Razorpay order id — created when payment is initiated
    @Column(unique = true)
    private String razorpayOrderId;

    // Razorpay payment id — received after successful payment
    private String razorpayPaymentId;

    // Razorpay signature — received after successful payment
    private String razorpaySignature;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.INITIATED;

    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String razorpayRefundId;
    private LocalDateTime RefundedAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}