package com.demo.dto;

import com.demo.enums.OrderStatus;
import com.demo.enums.PaymentStatus;
import com.demo.enums.PickupStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String orderNumber;
    private String userId;
    private String userEmail;
    private List<OrderItemDto> items;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PickupStatus pickupStatus;
    private String paymentMethod;
    private String note;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String cancellationReason;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}