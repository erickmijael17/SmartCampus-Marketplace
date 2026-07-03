package com.upeu.pagos.client.orden;

import java.math.BigDecimal;

public record OrdenClientRequest(
        Long idComprador,
        Long idProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        String estado
) {
}
