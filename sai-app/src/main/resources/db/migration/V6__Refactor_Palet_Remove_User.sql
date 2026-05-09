-- Eliminar la relación directa usuario ↔ palet.
-- El operador queda vinculado al palet exclusivamente a través de pallet_scans.workshift.user.

ALTER TABLE palets DROP FOREIGN KEY FKbxrpgfqk5fxwollt0ymdrs6su;
ALTER TABLE palets DROP INDEX idx_palet_user;
ALTER TABLE palets DROP COLUMN employee_number;

-- Ampliar el enum scan_quality para incluir creaciones manuales vía REST.
ALTER TABLE pallet_scans MODIFY COLUMN scan_quality ENUM('PERFECT', 'MERGED', 'CAM1_ONLY', 'CAM2_ONLY', 'MANUAL') NOT NULL;
