-- Insertar usuarios de prueba (1 ADMIN, 3 OPERATOR)
INSERT INTO users (active, blocked, create_at, email, employee_number,
                   expired, job_position, name, password, registration_date, role, surname)
VALUES
    -- Administrador
    (1, 0, CURRENT_TIMESTAMP(6), 'carlos.admin@yopmail.com', '1000',
     0, 'Jefe de Planta', 'Carlos', '$2a$12$77xGyLvF9cAwQGlj8wVs5up3YLEWe6cgm12EKB2lKID5UKvAQh12q', CURRENT_DATE, 'ADMIN', 'Pascual'),

    -- Operadores
    (1, 0, CURRENT_TIMESTAMP(6), 'laura.gomez@yopmail.com', '1001',
     0, 'Operario de Cinta', 'Laura', '$2a$12$oXX8VUo7miqSFsD6TpJgyO8cbpD2Izdki0KUZFsh8tKH5c9ornN3O', CURRENT_DATE, 'OPERATOR', 'Gomez'),

    (1, 0, CURRENT_TIMESTAMP(6), 'david.martin@yopmail.com', '1002',
     0, 'Operario de Muelle', 'David', '$2a$12$oXX8VUo7miqSFsD6TpJgyO8cbpD2Izdki0KUZFsh8tKH5c9ornN3O', CURRENT_DATE, 'OPERATOR', 'Martin'),

    (1, 0, CURRENT_TIMESTAMP(6), 'elena.rodriguez@yopmail.com', '1003',
     0, 'Operario de Cinta', 'Elena', '$2a$12$oXX8VUo7miqSFsD6TpJgyO8cbpD2Izdki0KUZFsh8tKH5c9ornN3O', CURRENT_DATE, 'OPERATOR', 'Rodriguez');