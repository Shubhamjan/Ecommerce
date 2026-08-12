package com.demo.dto;

import com.demo.enums.PickupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupStatusUpdateRequest {

    @NotNull(message = "Pickup status is required")
    private PickupStatus pickupStatus;
}