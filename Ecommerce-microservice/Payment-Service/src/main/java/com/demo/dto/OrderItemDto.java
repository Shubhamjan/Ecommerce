package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private String brand;
    private double price;
    private double discountedPrice;
    private int quantity;
    private double totalPrice;
}