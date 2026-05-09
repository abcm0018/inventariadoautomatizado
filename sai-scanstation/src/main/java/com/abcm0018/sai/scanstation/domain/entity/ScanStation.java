package com.abcm0018.sai.scanstation.domain.entity;

import com.abcm0018.sai.scanstation.domain.enums.ScanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "SCAN_STATIONS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_station_code", columnNames = {"STATION_CODE"})
        }
)
public class ScanStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STATION_CODE", nullable = false, length = 50)
    private String stationCode;

    @Column(name = "LOCATION_DESC", length = 20)
    private String locationDesc;

    @Column(name = "CAMERA_1_ID", unique = true, length = 20)
    private String camera1Id;

    @Column(name = "CAMERA_2_ID", unique = true, length = 20)
    private String camera2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ScanStatus status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // ========== CICLO DE VIDA ==========

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;

        if (this.status == null) {
            this.status = ScanStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== MÉTODOS DE NEGOCIO ==========

    public boolean isActive() {
        return this.status == ScanStatus.ACTIVE;
    }

    public boolean isOffline() {
        return this.status == ScanStatus.OFFLINE;
    }

    public boolean isInMaintenance() {
        return this.status == ScanStatus.MAINTENANCE;
    }
}