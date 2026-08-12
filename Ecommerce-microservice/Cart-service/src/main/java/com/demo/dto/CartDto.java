package com.demo.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private int totalItems;
    private double totalAmount;
    private LocalDateTime updatedAt;
}