package com.abcm0018.inventarioautomatizado.palets.domain.entity;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PALET")
public class Palet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ean", nullable = false, length = 14)
    private String ean;
    @Column(name = "batch_number", nullable = false)
    private String batchNumber; //lote
    @Column(name = "packaging_date", nullable = false)
    private LocalDate packagingDate;
    @Column(name = "product_use_by_date", nullable = false)
    private LocalDate productUseByDate;
    @Column(name = "production_time", nullable = false)
    private String productionTime;
    @Column(name = "sscc", nullable = false, length = 18, unique = true)
    private String sscc;
    @Column(name = "shift", nullable = false)
    private String shift;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_number", referencedColumnName = "employee_number")
    private User user;
}
