package com.demo.client;

import com.demo.dto.CartDto;
import com.demo.dto.CartItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Cart-Service")
public interface CartClient {

    @GetMapping("/cart")
    CartDto getCart(@RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/cart/items/{cartItemId}")
    CartItemDto getCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId);

    @DeleteMapping("/api/cart/items/{cartItemId}")
    void removeCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId);

    @DeleteMapping("/api/cart/clear")
    void clearCart(@RequestHeader("X-User-Id") String userId);
}