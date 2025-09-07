CREATE TABLE PALET (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ean VARCHAR(20) NOT NULL UNIQUE,
    sscc VARCHAR(20) NOT NULL UNIQUE,
    batch_number VARCHAR(50) NOT NULL, -- Lote
    production_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    status ENUM('en_almacen', 'en_transito', 'bloqueado') DEFAULT 'en_almacen',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED,
    CONSTRAINT fk_palets_user FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
