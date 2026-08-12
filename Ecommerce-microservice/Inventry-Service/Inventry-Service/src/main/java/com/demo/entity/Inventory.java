package com.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    @Column(unique = true,nullable = false)
//    private String skuCode;//stock keeper code
    @Column(unique = true, nullable = false)
    private Long productId;//stock keeper code

    @Column(nullable = false)
    private Integer quantity;
}
