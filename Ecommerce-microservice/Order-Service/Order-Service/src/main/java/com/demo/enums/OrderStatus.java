package com.demo.enums;

public enum OrderStatus {
    PENDING,        // order placed
    PLACED,      // confirmed by shop
    PROCESSING,     // being prepared
    READY,          // ready for pickup
    COMPLETED,      // picked up by customer
    CANCELLED       // cancelled
}
