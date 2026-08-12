package com.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {

//    @NotBlank(message = "SKU code must not be black")
//    private String skuCode;
    private Long productId;

    @Min( value = 0, message = "Quantity must be 0 or more")
    private Integer quantity;

    private String operation;
}
