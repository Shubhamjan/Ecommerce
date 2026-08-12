package com.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Inventory-Service")
public interface InventoryClient {

    @GetMapping("/api/inventory/check/{productId}/{quantity}")
    boolean isInStock(@PathVariable Long productId,
                      @PathVariable int quantity);

    @PutMapping("/inventory/reduce/{productId}/{quantity}")
    void reduceStock(@PathVariable Long productId,
                     @PathVariable int quantity);
}