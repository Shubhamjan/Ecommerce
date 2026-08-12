package com.demo.mapper;

import com.demo.dto.CartDto;
import com.demo.dto.CartItemDto;
import com.demo.entity.Cart;
import com.demo.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::mapToCartItemDto)
                .collect(Collectors.toList());

        double totalAmount = itemDtos.stream()
                .mapToDouble(CartItemDto::getTotalPrice)
                .sum();

        int totalItems = itemDtos.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemDtos)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public CartItemDto mapToCartItemDto(CartItem item) {
        return CartItemDto.builder()
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
