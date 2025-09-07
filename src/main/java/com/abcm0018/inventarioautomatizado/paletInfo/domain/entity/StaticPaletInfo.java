package com.abcm0018.inventarioautomatizado.paletInfo.domain.entity;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.palets.domain.entity.PaletStatus;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "STATIC_PALET_INFO")
public class StaticPaletInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sscc;
    private BigDecimal weight;
    private Integer boxesPerPalet;
    private String stackingLimit; // Apilado máximo
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
