package com.demo.service.impl;

import com.demo.Exceptions.CartNotFoundException;
import com.demo.Exceptions.ProductNotFoundException;
import com.demo.Exceptions.ServiceUnavailableException;
import com.demo.client.ProductClient;
import com.demo.dto.*;
import com.demo.entity.Cart;
import com.demo.entity.CartItem;
import com.demo.mapper.CartMapper;
import com.demo.repository.CartItemRepository;
import com.demo.repository.CartRepository;
import com.demo.service.CartService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;
    private final CartItemRepository cartItemRepository;

    private final ProductServiceClient productServiceClient;

    @Override
    public CartDto getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        return cartMapper.mapToCartDto(cart);
    }

    // get or create cart for user
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).build()
                ));
    }

    @Override
    public CartDto addToCart(Long userId, AddToCartRequest request) {

        //Fetch product from Product service via feign

        ProductDto product;
//        try {
//            product = productClient.getProductById(request.getProductId());
//            log.info("The product from product service : -"+product.getName());
//        }catch (FeignException.NotFound ex) {
//            throw new ProductNotFoundException(
//                    "Product not found with id: " + request.getProductId());
//
//        } catch (FeignException ex)
//            throw new RuntimeException(
//                    "Product service error: " + ex.status());
//        }
        product = productServiceClient.fetchProduct(request.getProductId());


        // check stock availability
        if (product.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.getQuantity());
        }

        Cart cart = getOrCreateCart(userId);

        // check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            // update quantity if already exists
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();

            if (product.getQuantity() < newQty) {
                throw new IllegalArgumentException(
                        "Insufficient stock. Available: " + product.getQuantity());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            // add new item
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .brand(product.getBrand())
                    .price(product.getPrice())
                    .discountedPrice(product.getDiscountedPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
        return cartMapper.mapToCartDto(cart);
    }


    @Override
    public CartDto updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart item not found with id: " + cartItemId));

        // validate stock
//        ProductDto product = productClient.getProductById(item.getProductId());
        ProductDto product = productServiceClient.fetchProduct(item.getProductId());
        if (product.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.getQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return cartMapper.mapToCartDto(cart);
    }

    @Override
    public CartDto removeItem(Long userId, Long cartItemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart item not found with id: " + cartItemId));

        cart.removeItem(item);
        cartRepository.save(cart);
        return cartMapper.mapToCartDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for user: " + userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public Integer getCartItemCount(Long id) {

        Cart cart = cartRepository.findByUserId(id)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for user: " + id));

        int count = 0;
        for (CartItem cartItem : cart.getItems()) {
            count = count + cartItem.getQuantity();
        }
        return count;
    }

    @Override
    public CartItemDto getCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart item not found: " + cartItemId));

        return cartMapper.mapToCartItemDto(item);
    }

    //helper method

}
