-- V1: Índice de búsqueda (copia desnormalizada de publicaciones)
CREATE TABLE indice_busqueda (
    id              BIGSERIAL PRIMARY KEY,
    publicacion_id  BIGINT NOT NULL UNIQUE,
    titulo          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    precio          DECIMAL(10,2),
    categoria_id    BIGINT,
    categoria_nombre VARCHAR(100),
    vendedor_id     BIGINT,
    estado          VARCHAR(30),
    busqueda_fts    TSVECTOR,
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_busqueda_fts ON indice_busqueda USING GIN(busqueda_fts);
CREATE INDEX idx_busqueda_precio ON indice_busqueda(precio);
CREATE INDEX idx_busqueda_categoria ON indice_busqueda(categoria_id);

-- Trigger para mantener el vector de búsqueda actualizado
CREATE FUNCTION update_busqueda_fts() RETURNS TRIGGER AS $$
BEGIN
  NEW.busqueda_fts = to_tsvector('spanish', COALESCE(NEW.titulo,'') || ' ' || COALESCE(NEW.descripcion,''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_busqueda_fts
BEFORE INSERT OR UPDATE ON indice_busqueda
FOR EACH ROW EXECUTE FUNCTION update_busqueda_fts();
