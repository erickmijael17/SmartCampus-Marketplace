ALTER TABLE pagos
    ADD COLUMN IF NOT EXISTS fecha_confirmacion TIMESTAMP,
    ADD COLUMN IF NOT EXISTS evento_pago_aprobado_publicado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_pagos_mp_payment_id
    ON pagos (mp_payment_id)
    WHERE mp_payment_id IS NOT NULL;
