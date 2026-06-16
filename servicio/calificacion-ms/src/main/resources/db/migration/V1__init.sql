-- V1: Tabla de calificaciones/reseñas
CREATE TABLE calificaciones (
    id               BIGSERIAL PRIMARY KEY,
    publicacion_id   BIGINT NOT NULL,
    evaluador_id     BIGINT NOT NULL,
    evaluado_id      BIGINT NOT NULL,
    orden_id         BIGINT,
    puntuacion       SMALLINT NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
    comentario       TEXT,
    tipo             VARCHAR(20) NOT NULL CHECK (tipo IN ('VENDEDOR','COMPRADOR','PRODUCTO')),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_calificaciones_publicacion ON calificaciones(publicacion_id);
CREATE INDEX idx_calificaciones_evaluado ON calificaciones(evaluado_id);
CREATE UNIQUE INDEX idx_calificaciones_unica ON calificaciones(orden_id, evaluador_id, tipo) WHERE orden_id IS NOT NULL;
