package com.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "order")
//@EqualsAndHashCode(exclude = "order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long productId;
    private String productName;
    private String imageUrl;
    private String brand;
    private double price;
    private double discountedPrice;
    private int quantity;

    public double getTotalPrice() {
        double effectivePrice = discountedPrice > 0 ? discountedPrice : price;
        return effectivePrice * quantity;
    }
}