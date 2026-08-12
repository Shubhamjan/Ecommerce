package com.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    private String imageUrl;
    private String brand;

    @Column(nullable = false)
    private double price;          // price at time of adding

    private double discountedPrice;

    @Column(nullable = false)
    private int quantity;

    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        addedAt = LocalDateTime.now();
    }

    // calculated field
    public double getTotalPrice() {
        double effectivePrice = discountedPrice > 0 ? discountedPrice : price;
        return effectivePrice * quantity;
    }
}
