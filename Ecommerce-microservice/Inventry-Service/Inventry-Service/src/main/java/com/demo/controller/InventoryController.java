package com.demo.controller;

import com.demo.dto.InventoryRequest;
import com.demo.dto.InventoryResponse;
import com.demo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/check/{productId}/{quantity}")
//    public ResponseEntity<InventoryResponse> isInStock(@PathVariable Long productId,@PathVariable int quantity){
    public ResponseEntity<Boolean> isInStock(@PathVariable Long productId,@PathVariable int quantity){

        return ResponseEntity.ok(inventoryService.checkInventory(productId,quantity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> addInventory(@RequestBody InventoryRequest inventoryRequest){
        inventoryService.addInventory(inventoryRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("Inventory added successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public ResponseEntity<InventoryResponse> updateStock(@RequestBody InventoryRequest inventoryRequest){

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.updateStock(inventoryRequest));
    }
}
