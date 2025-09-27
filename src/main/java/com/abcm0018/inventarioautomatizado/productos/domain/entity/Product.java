package com.abcm0018.inventarioautomatizado.productos.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ean", nullable = false, length = 14, unique = true)
    private String ean;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "brand", nullable = false)
    private String brand;
    @Column(name = "description")
    private String description;
    @Column(name = "manufactured_in", nullable = false)
    private String manufacturedIn;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
