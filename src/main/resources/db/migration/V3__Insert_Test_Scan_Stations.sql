-- Insertar 4 estaciones de escaneo de prueba
INSERT INTO scan_stations (station_code, location_desc, camera_1_id, camera_2_id, status)
VALUES
    ('ST-IN-01', 'Entrada Producción - Línea A', 'CAM-IN-01-A', 'CAM-IN-01-B', 'ACTIVE'),
    ('ST-IN-02', 'Entrada Producción - Línea B', 'CAM-IN-02-A', 'CAM-IN-02-B', 'ACTIVE'),
    ('ST-OUT-01', 'Salida Logística - Muelle 1', 'CAM-OUT-01-A', 'CAM-OUT-01-B', 'ACTIVE'),
    ('ST-OUT-02', 'Salida Logística - Muelle 2', 'CAM-OUT-02-A', 'CAM-OUT-02-B', 'MAINTENANCE');