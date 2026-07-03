ALTER TABLE ordenes
    ADD COLUMN IF NOT EXISTS metodo_pago VARCHAR(30),
    ADD COLUMN IF NOT EXISTS id_vendedor BIGINT,
    ADD COLUMN IF NOT EXISTS pago_id BIGINT,
    ADD COLUMN IF NOT EXISTS fecha_venta TIMESTAMP,
    ADD COLUMN IF NOT EXISTS venta_confirmada_publicada BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE ordenes
SET venta_confirmada_publicada = FALSE
WHERE venta_confirmada_publicada IS NULL;

CREATE INDEX IF NOT EXISTS idx_ordenes_pago_id ON ordenes (pago_id);
