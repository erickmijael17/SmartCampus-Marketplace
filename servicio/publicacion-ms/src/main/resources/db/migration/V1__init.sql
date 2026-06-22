-- V1: Tabla de publicaciones del marketplace
CREATE TABLE publicaciones (
    id              BIGSERIAL PRIMARY KEY,
    titulo          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    precio          DECIMAL(10,2) NOT NULL,
    estado          VARCHAR(30) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','PAUSADO','VENDIDO','ELIMINADO')),
    condicion       VARCHAR(30) CHECK (condicion IN ('NUEVO','COMO_NUEVO','USADO','PARA_REPARAR')),
    categoria_id    BIGINT,
    vendedor_id     BIGINT NOT NULL,
    stock           INT NOT NULL DEFAULT 1,
    ubicacion       VARCHAR(200),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_publicaciones_vendedor ON publicaciones(vendedor_id);
CREATE INDEX idx_publicaciones_categoria ON publicaciones(categoria_id);
CREATE INDEX idx_publicaciones_estado ON publicaciones(estado);
