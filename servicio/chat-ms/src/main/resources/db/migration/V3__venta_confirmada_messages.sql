ALTER TABLE mensajes
    ADD COLUMN IF NOT EXISTS receptor_id BIGINT;

CREATE UNIQUE INDEX IF NOT EXISTS ux_mensajes_venta_confirmada
    ON mensajes (id_conversacion, id_orden, pago_id, tipo_mensaje)
    WHERE tipo_mensaje IN ('VENTA_CONFIRMADA_VENDEDOR', 'VENTA_CONFIRMADA_COMPRADOR');
