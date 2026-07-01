package com.upeu.ordenes.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrdenTest {

    @Test
    void setsCreatedAtBeforePersistingWhenItIsMissing() {
        Orden orden = Orden.builder()
                .idComprador(8L)
                .idProducto(99L)
                .cantidad(1)
                .precioUnitario(BigDecimal.valueOf(20))
                .estado("PENDIENTE")
                .build();

        orden.prePersist();

        assertThat(orden.getCreatedAt()).isNotNull();
    }
}
