-- Eliminar la redundancia entre palets y pallet_scans.
-- El contexto del turno (workshift) y el timestamp de escaneo (scanned_at)
-- pasan a ser responsabilidad exclusiva de la tabla pallet_scans.

ALTER TABLE palets DROP FOREIGN KEY FKr9c03vhbais69hb5k7000u4st;
ALTER TABLE palets DROP INDEX idx_palet_workshift;
ALTER TABLE palets DROP COLUMN workshift_id;
ALTER TABLE palets DROP COLUMN scanned_at;
