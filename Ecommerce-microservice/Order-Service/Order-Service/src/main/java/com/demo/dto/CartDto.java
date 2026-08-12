package com.demo.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long id;
    private String userId;
    private List<CartItemDto> items;
    private int totalItems;
    private double totalAmount;
}