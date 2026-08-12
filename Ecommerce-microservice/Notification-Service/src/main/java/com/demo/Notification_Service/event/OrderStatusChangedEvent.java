package com.demo.Notification_Service.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusChangedEvent {
    private String orderNumber;
    private String userId;
    private String userEmail;
    private String orderStatus;
    private String pickupStatus;
}