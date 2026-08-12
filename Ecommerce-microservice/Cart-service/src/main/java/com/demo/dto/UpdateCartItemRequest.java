package com.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}