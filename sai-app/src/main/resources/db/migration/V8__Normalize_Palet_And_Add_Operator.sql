-- =============================================================================
-- V8: Normalización de entidades Palet y PalletScan
--
-- Cambio 1: Unifica PACKAGIN_DATE (LocalDate) + PRODUCTION_TIME (String HH:mm)
--           en una sola columna PACKAGING_DATETIME (DATETIME), eliminando la
--           partición de un instante temporal único en dos atributos separados.
--
-- Cambio 2: Añade OPERATOR_ID en pallet_scans para trazabilidad directa al
--           operario que realizó o registró el escaneo, sin necesidad de
--           traversar workshift -> user en auditorías.
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- CAMBIO 1: Unificar fecha+hora de envasado en PACKAGING_DATETIME
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. Añadir nueva columna como nullable para poder poblarla antes de aplicar NOT NULL
ALTER TABLE PALETS ADD COLUMN PACKAGING_DATETIME DATETIME NULL;

-- 2. Migrar datos existentes: combinar PACKAGIN_DATE y PRODUCTION_TIME (HH:mm)
UPDATE PALETS
SET PACKAGING_DATETIME = TIMESTAMP(PACKAGIN_DATE, STR_TO_DATE(PRODUCTION_TIME, '%H:%i'))
WHERE PACKAGIN_DATE IS NOT NULL AND PRODUCTION_TIME IS NOT NULL;

-- 3. Filas sin PRODUCTION_TIME: usar medianoche del PACKAGIN_DATE
UPDATE PALETS
SET PACKAGING_DATETIME = TIMESTAMP(PACKAGIN_DATE, '00:00:00')
WHERE PACKAGIN_DATE IS NOT NULL AND PRODUCTION_TIME IS NULL AND PACKAGING_DATETIME IS NULL;

-- 4. Aplicar NOT NULL
ALTER TABLE PALETS MODIFY COLUMN PACKAGING_DATETIME DATETIME NOT NULL;

-- 5. Eliminar índice antiguo antes de borrar la columna
DROP INDEX idx_palet_packaging_date ON PALETS;

-- 6. Eliminar columnas antiguas
ALTER TABLE PALETS DROP COLUMN PACKAGIN_DATE;
ALTER TABLE PALETS DROP COLUMN PRODUCTION_TIME;

-- 7. Crear nuevo índice para PACKAGING_DATETIME
CREATE INDEX idx_palet_packaging_datetime ON PALETS (PACKAGING_DATETIME);

-- ─────────────────────────────────────────────────────────────────────────────
-- CAMBIO 2: Añadir referencia directa al operario en pallet_scans
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE pallet_scans
    ADD COLUMN operator_id BIGINT NULL,
    ADD CONSTRAINT fk_pallet_scans_operator
        FOREIGN KEY (operator_id) REFERENCES users (id);

CREATE INDEX idx_pallet_scans_operator ON pallet_scans (operator_id);
