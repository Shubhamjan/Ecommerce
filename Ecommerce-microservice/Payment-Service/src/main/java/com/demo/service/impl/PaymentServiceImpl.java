package com.demo.service.impl;

import com.demo.client.OrderClient;
import com.demo.dto.*;
import com.demo.entity.Payment;
import com.demo.entity.enums.OrderStatus;
import com.demo.entity.enums.PaymentStatus;
import com.demo.event.PaymentFailedEvent;
import com.demo.event.PaymentSuccessEvent;
import com.demo.event.RefundCompletedEvent;
import com.demo.exception.PaymentNotFoundException;
import com.demo.kafka.PaymentEventProducer;
import com.demo.mapper.PaymentMapper;
import com.demo.repository.PaymentRepository;
import com.demo.service.PaymentService;

import com.razorpay.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(String userId, PaymentInitiateRequest request) throws Exception {

        // find payment record created by kafka consumer
        Payment payment = paymentRepository.findByOrderNumber(request.getOrderNumber()).orElseThrow(() -> new PaymentNotFoundException(
                "Payment record not found for order: "
                        + request.getOrderNumber()));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Payment already completed for order: " + request.getOrderNumber());
        }

        // create Razorpay order
        // amount in paise (multiply by 100)

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", (int) (request.getAmount() * 100));
        orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
        orderRequest.put("receipt", request.getOrderNumber());
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // update payment record with razorpay order id
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        paymentRepository.save(payment);

        log.info("Payment initiated for order: {} razorpayOrderId: {}",
                request.getOrderNumber(), razorpayOrder.get("id").toString());

        return PaymentInitiateResponse.builder()
                .razorpayOrderId(razorpayOrder.get("id"))
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .keyId(razorpayKeyId)
                .build();
    }

    @Override
    @Transactional
    public PaymentDto verifyPayment(PaymentVerifyRequest request) throws Exception {

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for razorpay order: "
                                + request.getRazorpayOrderId()));

        // Idempotency guard — webhook may have already processed this
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

            log.info("Payment already processed for order: {}, status: {}",
                    payment.getOrderNumber(), payment.getPaymentStatus());
            return paymentMapper.mapToPaymentDto(payment);
        }

        // verify signature
        // signature = HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId,
        //             keySecret)
        String generatedSignature = generateSignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId());


        if (generatedSignature.equals(request.getRazorpaySignature())) {

            payment.setRazorpaySignature(request.getRazorpaySignature());
            processPaymentSuccess(payment, request.getRazorpayPaymentId());
        } else {
            processPaymentFailure(payment, "Invalid payment signature");
            throw new IllegalStateException("Payment verification failed");
        }
//
        return paymentMapper.mapToPaymentDto(payment);
    }


    @Override
    public PaymentDto getPaymentByOrderNumber(String orderNumber) {

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + orderNumber));

        return paymentMapper.mapToPaymentDto(payment);
    }

    @Override
    public Page<PaymentDto> getMyPayments(String userId, Pageable pageable) {

        return paymentRepository.findByUserId(userId, pageable)
                .map(paymentMapper::mapToPaymentDto);
    }

    @Override
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::mapToPaymentDto);
    }

    @Override
    @Transactional
    public PaymentDto refundPayment(String orderNumber) throws Exception {

        // ─────────────────────────────────────────────────────
        // STEP 1: Fetch payment record from our DB
        // ─────────────────────────────────────────────────────
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found for order: " + orderNumber));

        // ─────────────────────────────────────────────────────
        // STEP 2: Check if already refunded (idempotency check)
        // ─────────────────────────────────────────────────────
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Payment already refunded for order: " + orderNumber);
        }

        // ─────────────────────────────────────────────────────
        // STEP 3: Call Order Service via Feign Client
        //         to check if order is CANCELLED or not
        //         Refund is only allowed after cancellation
        // ─────────────────────────────────────────────────────
        OrderDto order = orderClient.getOrderByNumber(orderNumber);

        if (order.getOrderStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Refund not allowed. Order must be CANCELLED first. " +
                            "Current order status: " + order.getOrderStatus());
        }

        // ─────────────────────────────────────────────────────
        // STEP 4: Check payment status
        //
        //  Case 1: Order was PLACED and cancelled
        //          → payment status = PENDING
        //          → No payment was made, nothing to refund
        //
        //  Case 2: Order was READY and cancelled
        //          → payment status = SUCCESS
        //          → Payment was made, refund must be processed
        // ─────────────────────────────────────────────────────
        if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Refund not applicable for order: " + orderNumber +
                            ". No payment was made for this order.");
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Cannot refund payment with status: "
                            + payment.getPaymentStatus());
        }

        // ─────────────────────────────────────────────────────
        // STEP 5: All checks passed — Initiate Razorpay Refund
        //         Note: Razorpay refund is not instant.
        //         Razorpay returns status as "initiated"
        //         Actual credit to customer takes:
        //         UPI        → 2-3 business days
        //         Credit Card → 5-7 business days
        //         Net Banking → 3-5 business days
        //         Wallet      → Instant to 1 day
        // ─────────────────────────────────────────────────────
        try {

            log.info("Refund initiated for Razorpay Payment Id: {}",
                    payment.getRazorpayPaymentId());

            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(payment.getRazorpayPaymentId());

            log.info("Status   : {}", razorpayPayment.get("status").toString());
            log.info("Captured : {}", razorpayPayment.get("captured").toString());

// CAPTURE first if not captured
            if (!"captured".equals(razorpayPayment.get("status").toString())) {
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount", (int) Long.parseLong(razorpayPayment.get("amount").toString()));
                captureRequest.put("currency", "INR");
                razorpayClient.payments.capture(payment.getRazorpayPaymentId(), captureRequest);
                log.info("Payment captured successfully");
            }

// NOW refund
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int) Long.parseLong(razorpayPayment.get("amount").toString()));
            Refund refund = razorpayClient.payments.refund(
                    payment.getRazorpayPaymentId(), refundRequest);

            String razorpayRefundId = refund.get("id").toString();
            String refundStatus = refund.get("status").toString();

            log.info("Razorpay Refund Id: {}, Refund Status: {}", razorpayRefundId, refundStatus);

            // ─────────────────────────────────────────────────────
            // STEP 6: Update payment record in our DB
            //         We mark it REFUNDED meaning refund is
            //         successfully initiated from our side.
            //         Actual bank credit will take time.
            // ─────────────────────────────────────────────────────
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setRazorpayRefundId(razorpayRefundId);  // store refund ID
            payment.setRefundedAt(LocalDateTime.now());      // store refund timestamp

            paymentRepository.save(payment);

            log.info("Refund successfully initiated for order: {}", orderNumber);

            // ─────────────────────────────────────────────────────
            // STEP 7: Publish RefundCompletedEvent to Kafka
            //         → Notification Service consumes this event
            //         → Sends refund confirmation email to customer
            //           with expected credit timeline
            // ─────────────────────────────────────────────────────
            RefundCompletedEvent refundEvent = new RefundCompletedEvent(
                    orderNumber,
                    payment.getRazorpayPaymentId(),
                    razorpayRefundId,
                    payment.getAmount(),
                    payment.getUserEmail(),
                    LocalDateTime.now()
            );

            paymentEventProducer.publishRefundEvent(refundEvent);

            log.info("RefundCompletedEvent published for order: {}", orderNumber);

            return paymentMapper.mapToPaymentDto(payment);

        } catch (IllegalStateException e) {
            // re-throw business exceptions as-is
            throw e;

        } catch (Exception e) {

            log.error("Refund failed for order: {}", orderNumber, e);

            throw new RuntimeException(
                    "Refund failed for order: " + orderNumber, e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) throws Exception {

        // verify webhook signature
        boolean isValid = Utils.verifyWebhookSignature(payload, signature, razorpayKeySecret);

        if (!isValid) {
            throw new IllegalStateException("Invalid webhook signature");
        }

        JSONObject webhookData = new JSONObject(payload);

        String event = webhookData.getString("event");
        JSONObject paymentEntity = webhookData
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id");

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);


        if (payment == null) {
            log.warn("No payment found for razorpay order: {}", razorpayOrderId);
            return;
        }

// Idempotency guard — verifyPayment() may have already processed this
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already processed, skipping webhook. Order: {}, status: {}",
                    payment.getOrderNumber(), payment.getPaymentStatus());
            return;
        }
        switch (event) {

            case "payment.captured" -> processPaymentSuccess(payment, razorpayPaymentId);

            case "payment.failed" -> processPaymentFailure(payment,
                    paymentEntity.getJSONObject("error_description")
                            .optString("description", "Payment failed"));

            default -> log.info("Unhandled webhook event: {}", event);
        }
    }

    @Override
    public RefundStatusDto getRefundStatus(String orderNumber) {
        // Fetch from DB
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + orderNumber));

        if (payment.getRazorpayRefundId() == null) {
            throw new IllegalStateException(
                    "No refund initiated for order: " + orderNumber);
        }

        // Fetch live status from Razorpay
        try {
            Refund refund = razorpayClient.refunds.fetch(payment.getRazorpayRefundId());

            log.info("Refund ID     : {}", refund.get("id").toString());
            log.info("Status        : {}", refund.get("status").toString());
            log.info("Amount        : {}", refund.get("amount").toString());
            log.info("Speed         : {}", refund.get("speed_processed").toString());

            long amountInPaise = Long.parseLong(refund.get("amount").toString());
            return RefundStatusDto.builder()
                    .orderNumber(orderNumber)
                    .razorpayPaymentId(payment.getRazorpayPaymentId())
                    .razorpayRefundId(payment.getRazorpayRefundId())
                    .refundStatus(refund.get("status").toString())
                    .amountRefunded(amountInPaise/100.0)
                    .speed(refund.get("speed_processed") != null
                            ? refund.get("speed_processed").toString()
                            : "normal")
                    .refundedAt(payment.getRefundedAt())
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to fetch refund status for order: {}", orderNumber, e);
            throw new RuntimeException("Failed to fetch refund status", e);
        }
    }

    //*****************************************************************************************************************************************************************************************8
    //Helpers
    private String generateSignature(@NotBlank(message = "Razorpay order id is required") String razorpayOrderId,
                                     @NotBlank(message = "Razorpay payment id is required") String razorpayPaymentId) throws RazorpayException {

        String data = razorpayOrderId + "|" + razorpayPaymentId;
        return Utils.getHash(data, razorpayKeySecret);
    }

    private void processPaymentFailure(Payment payment, String reason) {

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);
        paymentEventProducer.publishPaymentFailed(
                PaymentFailedEvent.builder()
                        .orderNumber(payment.getOrderNumber())
                        .userId(payment.getUserId())
                        .userEmail(payment.getUserEmail())
                        .failureReason(reason)
                        .amount(payment.getAmount())
                        .build());
        log.warn("Payment failed for order: {}, reason: {}", payment.getOrderNumber(), reason);


    }

    private void processPaymentSuccess(Payment payment, @NotBlank(message = "Razorpay payment id is required") String razorpayPaymentId) {

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        paymentEventProducer.publishPaymentSuccess(
                PaymentSuccessEvent.builder()
                        .orderNumber(payment.getOrderNumber())
                        .userId(payment.getUserId())
                        .userEmail(payment.getUserEmail())
                        .razorpayPaymentId(razorpayPaymentId)
                        .amount(payment.getAmount())
                        .build());
        log.info("Payment successful for order: {}", payment.getOrderNumber());
    }

}
