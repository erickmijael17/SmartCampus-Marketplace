-- V1: Tabla de archivos multimedia
CREATE TABLE archivos (
    id              BIGSERIAL PRIMARY KEY,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacen  VARCHAR(255) NOT NULL UNIQUE,
    ruta            VARCHAR(500) NOT NULL,
    url             VARCHAR(1000),
    tipo_mime       VARCHAR(100) NOT NULL,
    tamanio         BIGINT NOT NULL,
    propietario_id  BIGINT NOT NULL,
    entidad_tipo    VARCHAR(50),
    entidad_id      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_archivos_propietario ON archivos(propietario_id);
CREATE INDEX idx_archivos_entidad ON archivos(entidad_tipo, entidad_id);
