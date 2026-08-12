package com.demo.controller;

import com.demo.dto.*;

import com.demo.enums.OrderStatus;
import com.demo.enums.PickupStatus;
import com.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── USER Endpoints ─────────────────────────────────

    // POST /orders — place order
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrder(userId, userEmail, request));
    }

    // GET /orders/my-orders — get my orders
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        return ResponseEntity.ok(orderService.getMyOrders(
            userId, PageRequest.of(page, size, sort)));
    }

    // GET /orders/{orderNumber} — get order details
    @GetMapping("/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable String orderNumber,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        return ResponseEntity.ok(
            orderService.getOrderByNumber(orderNumber, userId, isAdmin));
    }

    // PUT /orders/{orderNumber}/cancel — cancel order
    @PutMapping("/{orderNumber}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable String orderNumber,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(
            orderService.cancelOrder(orderNumber, userId, reason));
    }

    // ── ADMIN Endpoints ────────────────────────────────

    // GET /orders — get all orders
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        return ResponseEntity.ok(orderService.getAllOrders(
            PageRequest.of(page, size, sort)));
    }

    // PUT /orders/{orderNumber}/status — update order status
    @PutMapping("/{orderNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(
            orderService.updateOrderStatus(orderNumber, request));
    }

    // PUT /orders/{orderNumber}/pickup — update pickup status
    @PutMapping("/{orderNumber}/pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updatePickupStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody PickupStatusUpdateRequest request) {
        return ResponseEntity.ok(
            orderService.updatePickupStatus(orderNumber, request));
    }

    // GET /orders/filter/status — get orders by order status
    @GetMapping("/filter/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(
            status, PageRequest.of(page, size,
                Sort.by("createdAt").descending())));
    }

    // GET /orders/filter/pickup — get orders by pickup status
    @GetMapping("/filter/pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByPickupStatus(
            @RequestParam PickupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersByPickupStatus(
            status, PageRequest.of(page, size,
                Sort.by("createdAt").descending())));
    }
}