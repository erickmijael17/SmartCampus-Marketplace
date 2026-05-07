CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_orden BIGINT NOT NULL,
    id_comprador BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    referencia_transaccion VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_pagos_comprador (id_comprador),
    INDEX idx_pagos_orden (id_orden)
);

