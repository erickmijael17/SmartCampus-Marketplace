package com.upeu.pagos.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PagoTest {

    @Test
    void setsCreatedAtBeforePersistingWhenItIsMissing() {
        Pago pago = Pago.builder()
                .idOrden(12L)
                .idComprador(8L)
                .monto(BigDecimal.valueOf(100))
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .referenciaTransaccion("MP-PENDING")
                .build();

        pago.prePersist();

        assertThat(pago.getCreatedAt()).isNotNull();
    }
}
