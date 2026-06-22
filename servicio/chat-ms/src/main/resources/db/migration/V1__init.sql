-- V1: Tablas de chat entre usuarios
CREATE TABLE conversaciones (
    id              BIGSERIAL PRIMARY KEY,
    participante1   BIGINT NOT NULL,
    participante2   BIGINT NOT NULL,
    publicacion_id  BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (participante1, participante2, publicacion_id)
);

CREATE TABLE mensajes (
    id               BIGSERIAL PRIMARY KEY,
    conversacion_id  BIGINT NOT NULL REFERENCES conversaciones(id),
    remitente_id     BIGINT NOT NULL,
    contenido        TEXT NOT NULL,
    leido            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mensajes_conversacion ON mensajes(conversacion_id);
CREATE INDEX idx_mensajes_leido ON mensajes(conversacion_id, leido) WHERE NOT leido;
