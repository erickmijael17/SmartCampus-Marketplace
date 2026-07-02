ALTER TABLE conversacions
    ADD COLUMN IF NOT EXISTS publicacion_id BIGINT,
    ADD COLUMN IF NOT EXISTS id_orden BIGINT,
    ADD COLUMN IF NOT EXISTS tipo_chat VARCHAR(30);

ALTER TABLE mensajes
    ADD COLUMN IF NOT EXISTS tipo_remitente VARCHAR(30),
    ADD COLUMN IF NOT EXISTS tipo_mensaje VARCHAR(50),
    ADD COLUMN IF NOT EXISTS id_orden BIGINT,
    ADD COLUMN IF NOT EXISTS pago_id BIGINT,
    ADD COLUMN IF NOT EXISTS mp_payment_id VARCHAR(80);

CREATE UNIQUE INDEX IF NOT EXISTS ux_conversacions_id_orden
    ON conversacions (id_orden)
    WHERE id_orden IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_mensajes_confirmacion_pago
    ON mensajes (id_conversacion, id_orden, tipo_mensaje)
    WHERE tipo_mensaje = 'SISTEMA_CONFIRMACION_PAGO';

CREATE UNIQUE INDEX IF NOT EXISTS ux_mensajes_venta_validada
    ON mensajes (id_conversacion, id_orden, pago_id, mp_payment_id, tipo_mensaje)
    WHERE tipo_mensaje = 'VENTA_VALIDADA';
