package com.abcm0018.inventarioautomatizado.palets.domain.entity;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


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
    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;
    @Column(name = "time", nullable = false)
    private String time;
    @Column(name = "sscc", nullable = false, length = 18, unique = true)
    private String sscc;
    @Column(name = "shift", nullable = false)
    private String shift;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    private Product product;
}
