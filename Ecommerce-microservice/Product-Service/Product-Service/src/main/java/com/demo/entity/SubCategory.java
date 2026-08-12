package com.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="category_id",nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "subCategory",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> productList;
}
