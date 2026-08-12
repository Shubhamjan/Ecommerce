package com.demo.service;

import com.demo.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentInitiateResponse initiatePayment(String userId,
                                            PaymentInitiateRequest request) throws Exception;

    PaymentDto verifyPayment(PaymentVerifyRequest request) throws Exception;

    PaymentDto getPaymentByOrderNumber(String orderNumber);

    Page<PaymentDto> getMyPayments(String userId, Pageable pageable);

    Page<PaymentDto> getAllPayments(Pageable pageable);

    PaymentDto refundPayment(String orderNumber) throws Exception;

    void handleWebhook(String payload, String signature) throws Exception;

    RefundStatusDto getRefundStatus(String orderNumber);
}