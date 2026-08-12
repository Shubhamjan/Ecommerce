package com.demo.controller;

import com.demo.dto.AddToCartRequest;
import com.demo.dto.CartDto;
import com.demo.dto.CartItemDto;
import com.demo.dto.UpdateCartItemRequest;
import com.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestHeader("X-User-Id") String userId) {

        Long id = Long.parseLong(userId);

        return ResponseEntity.ok(cartService.getCart(id));
    }

    // POST /cart/add — add item to cart
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@RequestHeader("X-User-Id") String userId,
                                             @Valid @RequestBody AddToCartRequest request) {
        Long id = Long.parseLong(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(id, request));

    }

    // PUT /cart/items/{cartItemId} — update item quantity
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> updateItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Long id = Long.parseLong(userId);
        return ResponseEntity.ok(cartService.updateCartItem(id,cartItemId,request));
    }

    // DELETE /cart/items/{cartItemId} — remove single item
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId) {
        Long id = Long.parseLong(userId);
        return ResponseEntity.ok(cartService.removeItem(id, cartItemId));
    }

    // DELETE /cart/clear — remove all items
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") String userId) {

        Long id = Long.parseLong(userId);
        cartService.clearCart(id);
        return ResponseEntity.noContent().build();
    }

    // GET /cart/count — get total item count (for badge in UI)
    @GetMapping("/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Integer> getCartCount(
            @RequestHeader("X-User-Id") String userId) {

        Long id = Long.parseLong(userId);
        return ResponseEntity.ok(cartService.getCartItemCount(id));
    }

    // GET /cart/items/{cartItemId} — get single cart item
    @GetMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartItemDto> getCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId) {

        Long id = Long.parseLong(userId);
        return ResponseEntity.ok(cartService.getCartItem(id, cartItemId));
    }
}
