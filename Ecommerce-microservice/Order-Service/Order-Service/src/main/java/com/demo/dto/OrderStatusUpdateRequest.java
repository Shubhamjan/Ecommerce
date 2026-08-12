package com.demo.dto;


import com.demo.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    private String cancellationReason;
}