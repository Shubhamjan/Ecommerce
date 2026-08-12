package com.demo.controller;

import com.demo.dto.*;
import com.demo.entity.Payment;
import com.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final PaymentService paymentService;

    // ── USER Endpoints ─────────────────────────────────

    // POST /payments/initiate — create razorpay order
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PaymentInitiateRequest request
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(userId, request));
    }

    // POST /payments/verify — verify payment after frontend callback
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentDto> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request
    ) throws Exception {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/refund/{orderId}")
    public ResponseEntity<PaymentDto> refund(@PathVariable String orderId) throws Exception {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }

    // GET /payments/order/{orderNumber} — get payment by order number
    @GetMapping("/order/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentByOrderNumber(@PathVariable String orderNumber){

        return ResponseEntity.ok(paymentService.getPaymentByOrderNumber(orderNumber));
    }

    // GET /payments/my-payments — get logged in user's payments
    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PaymentDto>> getMyPayments(@RequestHeader("X-User-Id")String userId,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10")int size){
        return ResponseEntity.ok(paymentService.getMyPayments(
                userId, PageRequest.of(page, size,
                        Sort.by("createdAt").descending())));
    }

    // ── ADMIN Endpoints ────────────────────────────────

    // GET /payments — get all payments
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentDto>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getAllPayments(
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending())));
    }

    // POST /payments/webhook — razorpay webhook (no auth)
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature)
            throws Exception {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/refund-status/{orderNumber}")
    public ResponseEntity<RefundStatusDto> getRefundStatus(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getRefundStatus(orderNumber));
    }
}
