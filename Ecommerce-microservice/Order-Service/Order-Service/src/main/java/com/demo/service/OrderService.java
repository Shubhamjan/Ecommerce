package com.demo.service;

import com.demo.dto.*;
import com.demo.enums.OrderStatus;
import com.demo.enums.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDto placeOrder(String userId, String userEmail,
                        PlaceOrderRequest request);
    OrderDto getOrderByNumber(String orderNumber, String userId,
                              boolean isAdmin);
    Page<OrderDto> getMyOrders(String userId, Pageable pageable);
    OrderDto cancelOrder(String orderNumber, String userId, String reason);
    Page<OrderDto> getAllOrders(Pageable pageable);
    OrderDto updateOrderStatus(String orderNumber,
                               OrderStatusUpdateRequest request);
    OrderDto updatePickupStatus(String orderNumber,
                                PickupStatusUpdateRequest request);
    Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable);
    Page<OrderDto> getOrdersByPickupStatus(PickupStatus status,
                                           Pageable pageable);



}
