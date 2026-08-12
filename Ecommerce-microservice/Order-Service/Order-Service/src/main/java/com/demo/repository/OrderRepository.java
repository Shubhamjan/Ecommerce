package com.demo.repository;

import com.demo.entity.Order;

import com.demo.enums.OrderStatus;
import com.demo.enums.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserId(String userId, Pageable pageable);
    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByPickupStatus(PickupStatus status, Pageable pageable);
}