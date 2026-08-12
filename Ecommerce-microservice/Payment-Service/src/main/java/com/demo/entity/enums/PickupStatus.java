package com.demo.entity.enums;

public enum PickupStatus {
    WAITING,     // waiting for preparation
    READY,       // ready for pickup
    PICKED_UP,   // customer picked up
    EXPIRED      // not picked up in time
}