CREATE TABLE IF NOT EXISTS ordenes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_comprador BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_ordenes_comprador (id_comprador),
    INDEX idx_ordenes_producto (id_producto)
);

