-- =============================================================================
-- V9: Añadir valor INACTIVE al ENUM status de scan_stations
--
-- El enum Java ScanStatus incluye INACTIVE pero el DDL original (V2) solo
-- declaraba ('ACTIVE', 'MAINTENANCE', 'OFFLINE'). Esta migración sincroniza
-- la columna con el enum de la aplicación.
-- =============================================================================

ALTER TABLE scan_stations
    MODIFY COLUMN status ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE') DEFAULT 'ACTIVE';
