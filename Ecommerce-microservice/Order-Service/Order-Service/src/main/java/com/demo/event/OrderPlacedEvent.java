package com.demo.event;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {
    private String orderNumber;
    private String userId;
    private String userEmail;
    private List<OrderItemEvent> items;
    private double finalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemEvent {
        private Long productId;
        private int quantity;
    }
}