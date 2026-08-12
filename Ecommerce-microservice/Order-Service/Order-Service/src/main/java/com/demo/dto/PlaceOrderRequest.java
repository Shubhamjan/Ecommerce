package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotEmpty(message = "Please select at least one item to order")
    private List<SelectedCartItem> selectedItems;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String note;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedCartItem {
        private Long cartItemId;   // id of CartItem
        private int quantity;      // how many of that item to order
        // (can be less than cart quantity)
    }
}