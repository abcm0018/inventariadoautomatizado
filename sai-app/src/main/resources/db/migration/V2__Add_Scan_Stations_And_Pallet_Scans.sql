-- 1. Tabla Maestra de Puestos de Escaneo
CREATE TABLE scan_stations
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_code  VARCHAR(50) NOT NULL UNIQUE,
    location_desc VARCHAR(255),
    camera_1_id   VARCHAR(100),
    camera_2_id   VARCHAR(100),
    status        ENUM ('ACTIVE', 'MAINTENANCE', 'OFFLINE') DEFAULT 'ACTIVE',
    created_at    DATETIME(6)                               DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)                               DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- 2. Tabla Transaccional de Escaneos de Palets
CREATE TABLE pallet_scans
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    scanned_at   DATETIME(6)                                          NOT NULL,
    pallet_id    BIGINT,
    station_id   BIGINT,
    workshift_id BIGINT,
    scan_quality ENUM ('PERFECT', 'MERGED', 'CAM1_ONLY', 'CAM2_ONLY') NOT NULL,

    CONSTRAINT fk_pallet_scans_pallet FOREIGN KEY (pallet_id) REFERENCES palets (id),
    CONSTRAINT fk_pallet_scans_station FOREIGN KEY (station_id) REFERENCES scan_stations (id),
    CONSTRAINT fk_pallet_scans_workshift FOREIGN KEY (workshift_id) REFERENCES workshifts (id)
);

CREATE INDEX idx_pallet_scans_scanned_at ON pallet_scans (scanned_at);
CREATE INDEX idx_pallet_scans_pallet_id ON pallet_scans (pallet_id);