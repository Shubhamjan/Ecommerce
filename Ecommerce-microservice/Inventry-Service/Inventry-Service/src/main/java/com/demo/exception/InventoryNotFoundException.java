package com.demo.exception;

public class InventoryNotFoundException extends RuntimeException{

    public InventoryNotFoundException(Long skuCode){
        super("Inventory not found for skuCode "+skuCode);
    }
}
