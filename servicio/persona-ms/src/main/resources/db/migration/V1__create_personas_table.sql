-- V1: Creación de tabla personas
CREATE TABLE IF NOT EXISTS personas (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE,
    nombres              VARCHAR(100) NOT NULL,
    apellidos            VARCHAR(100) NOT NULL,
    email                VARCHAR(150) NOT NULL UNIQUE,
    telefono             VARCHAR(20),
    codigo_universitario VARCHAR(20) UNIQUE,
    tipo_usuario         VARCHAR(30) NOT NULL CHECK (tipo_usuario IN ('ESTUDIANTE','DOCENTE','ADMINISTRATIVO','EGRESADO')),
    carrera              VARCHAR(100),
    facultad             VARCHAR(100),
    foto_perfil_url      VARCHAR(500),
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_personas_user_id ON personas(user_id);
CREATE INDEX idx_personas_email ON personas(email);
CREATE INDEX idx_personas_codigo_universitario ON personas(codigo_universitario);
CREATE INDEX idx_personas_tipo_usuario ON personas(tipo_usuario);
