ALTER TABLE ordenes
    ADD COLUMN IF NOT EXISTS pago_id BIGINT,
    ADD COLUMN IF NOT EXISTS fecha_venta TIMESTAMP,
    ADD COLUMN IF NOT EXISTS venta_confirmada_publicada BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_ordenes_pago_id ON ordenes (pago_id);
