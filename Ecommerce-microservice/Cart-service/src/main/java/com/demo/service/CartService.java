package com.demo.service;

import com.demo.dto.AddToCartRequest;
import com.demo.dto.CartDto;
import com.demo.dto.CartItemDto;
import com.demo.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;

public interface CartService {
    CartDto getCart(Long userId);

    CartDto addToCart(Long userId, @Valid AddToCartRequest request);

    CartDto updateCartItem(Long id, Long cartItemId, @Valid UpdateCartItemRequest request);

    CartDto removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);

    Integer getCartItemCount(Long id);

    CartItemDto getCartItem(Long userId, Long cartItemId);
}
