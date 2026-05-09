package com.abcm0018.sai.palets.domain.entity;

import java.time.LocalDateTime;

import com.abcm0018.sai.palets.domain.enums.ScanQuality;
import com.abcm0018.sai.scanstation.domain.entity.ScanStation;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "pallet_scans",
    indexes = {
        @Index(name = "idx_pallet_scans_pallet_id",  columnList = "pallet_id"),
        @Index(name = "idx_pallet_scans_scanned_at", columnList = "scanned_at"),
        @Index(name = "idx_pallet_scans_operator",   columnList = "operator_id")
    }
)
public class PalletScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pallet_id", nullable = false)
    private Palet pallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private ScanStation station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshift_id")
    private Workshift workshift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_quality", nullable = false)
    private ScanQuality scanQuality;
}
