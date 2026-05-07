CREATE TABLE IF NOT EXISTS inventarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_producto BIGINT NOT NULL,
    stock_disponible INT NOT NULL,
    stock_reservado INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventarios_producto (id_producto)
);

