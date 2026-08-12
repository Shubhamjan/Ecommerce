package com.demo.service.impl;

import com.demo.Exceptions.ProductNotFoundException;
import com.demo.Exceptions.ServiceUnavailableException;
import com.demo.client.ProductClient;
import com.demo.dto.ProductDto;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public ProductDto fetchProduct(@NotNull(message = "Product id is required") Long productId) {

        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException("Product not found");
        }
    }

    public ProductDto productFallback(Long productId, Exception ex) {

        log.error("Fallback executed for productId: {}, reason: {}",
                productId,
                ex.getMessage());

        // if you want to stop cart operation
        throw new ServiceUnavailableException(
                "Product service is temporarily unavailable. Please try again later.");
    }
}
