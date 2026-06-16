-- V1: Tabla de categorías
CREATE TABLE categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    icono       VARCHAR(100),
    padre_id    BIGINT REFERENCES categorias(id),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    orden       INT DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
INSERT INTO categorias (nombre, descripcion, icono) VALUES
  ('Libros y Apuntes', 'Material académico', 'book'),
  ('Electrónica', 'Dispositivos y accesorios', 'laptop'),
  ('Ropa', 'Vestimenta universitaria', 'shirt'),
  ('Servicios', 'Tutorías y servicios estudiantiles', 'tools'),
  ('Alimentos', 'Comida y bebidas', 'food'),
  ('Otros', 'Artículos varios', 'box');
