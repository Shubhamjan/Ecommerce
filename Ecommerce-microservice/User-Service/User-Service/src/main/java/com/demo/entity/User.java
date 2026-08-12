package com.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String roles;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePresist(){
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = Instant.now();
    }

}
