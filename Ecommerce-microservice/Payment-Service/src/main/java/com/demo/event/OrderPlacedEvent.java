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
    private double finalAmount;
    private List<OrderItemEvent> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemEvent {
        private Long productId;
        private int quantity;
    }
}