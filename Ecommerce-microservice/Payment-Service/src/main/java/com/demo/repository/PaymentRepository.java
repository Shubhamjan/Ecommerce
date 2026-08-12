package com.demo.repository;

import com.demo.entity.Payment;
import com.demo.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderNumber(String orderNumber);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Page<Payment> findByUserId(String userId, Pageable pageable);
    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);
}