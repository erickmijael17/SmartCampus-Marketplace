-- V1: Tabla de favoritos/wishlist
CREATE TABLE favoritos (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL,
    publicacion_id  BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (usuario_id, publicacion_id)
);
CREATE INDEX idx_favoritos_usuario ON favoritos(usuario_id);
