package com.abcm0018.sai.palets.domain.entity;

import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"productPackLevel", "scans"})
@Entity
@NoArgsConstructor
@Table(name = "PALETS",
        uniqueConstraints = {@UniqueConstraint(name = "uk_palet_sscc", columnNames = {"SSCC"})},
        indexes = {
            @Index(name = "idx_palet_sscc",           columnList = "SSCC"),
            @Index(name = "idx_palet_pack_level",      columnList = "PACK_LEVEL_ID"),
            @Index(name = "idx_palet_batch",              columnList = "BATCH_NUMBER"),
            @Index(name = "idx_palet_packaging_datetime", columnList = "PACKAGING_DATETIME"),
            @Index(name = "idx_palet_expiry_date",        columnList = "PRODUCT_USE_BY_DATE"),
            @Index(name = "idx_palet_created_at",      columnList = "CREATE_AT")
        }
)
public class Palet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SSCC", nullable = false, length = 18, unique = true)
    private String sscc;

    @Column(name = "BATCH_NUMBER", nullable = false)
    private String batchNumber;

    /**
     * Relación principal: Palet → ProductPackLevel → Product.
     * Proporciona GTIN-14 e información logística del embalaje.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PACK_LEVEL_ID", nullable = false)
    private ProductPackLevel productPackLevel;

    @Column(name = "PACKAGING_DATETIME", nullable = false)
    private LocalDateTime packagingDateTime;

    @Column(name = "PRODUCT_USE_BY_DATE", nullable = false)
    private LocalDate productUseByDate;

    @Column(name = "CREATE_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATE_AT")
    private LocalDateTime updatedAt;

    /**
     * Historial de eventos de escaneo asociados a este palet.
     * Ordenado por scannedAt DESC: el primer elemento es el escaneo más reciente.
     * workshift y scannedAt viven exclusivamente aquí (no en Palet).
     */
    @OneToMany(mappedBy = "pallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("scannedAt DESC")
    private List<PalletScan> scans = new ArrayList<>();

    public static Palet createNewPalet(String sscc, String batchNumber, ProductPackLevel packLevel,
            LocalDateTime packagingDateTime, LocalDate productUseByDate) {

        if (sscc == null || packLevel == null) {
            throw new IllegalArgumentException("sscc y packLevel son obligatorios");
        }

        Palet palet = new Palet();
        palet.setSscc(sscc);
        palet.setBatchNumber(batchNumber);
        palet.setProductPackLevel(packLevel);
        palet.setPackagingDateTime(packagingDateTime);
        palet.setProductUseByDate(productUseByDate);
        return palet;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Acceso directo al último evento de escaneo (null si no hay ninguno). */
    public PalletScan getLatestScan() {
        return scans.isEmpty() ? null : scans.get(0);
    }

    public Product getProduct() {
        return this.productPackLevel.getProduct();
    }

    public String getGtin() {
        return this.productPackLevel.getGtin();
    }

    public boolean isExpired() {
        return this.productUseByDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        return this.productUseByDate.isBefore(LocalDate.now().plusDays(7));
    }

    public boolean isCriticalExpity() {
        return this.productUseByDate.isBefore(LocalDate.now().plusDays(3));
    }

    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), this.productUseByDate);
    }
}
