package com.demo.mapper;


import com.demo.dto.OrderDto;
import com.demo.dto.OrderItemDto;
import com.demo.entity.Order;
import com.demo.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    // ── Mappers ───────────────────────────────────────────

    public OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .items(itemDtos)
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .pickupStatus(order.getPickupStatus())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .cancellationReason(order.getCancellationReason())
                .readyAt(order.getReadyAt())
                .pickedUpAt(order.getPickedUpAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderItemDto mapToOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .imageUrl(item.getImageUrl())
                .brand(item.getBrand())
                .price(item.getPrice())
                .discountedPrice(item.getDiscountedPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }

}
