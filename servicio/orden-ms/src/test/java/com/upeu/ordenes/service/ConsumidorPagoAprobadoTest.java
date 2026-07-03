package com.upeu.ordenes.service;

import com.upeu.ordenes.evento.EventoPagoAprobado;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;

class ConsumidorPagoAprobadoTest {

    @Test
    void consumesApprovedPaymentEvent() {
        OrdenService ordenService = mock(OrdenService.class);
        ConsumidorPagoAprobado consumer = new ConsumidorPagoAprobado(ordenService);
        EventoPagoAprobado evento = EventoPagoAprobado.builder()
                .tipoEvento("pago.aprobado")
                .ordenId(83L)
                .pagoId(25L)
                .idComprador(8L)
                .idVendedor(2L)
                .productoId(77L)
                .build();

        consumer.consumir(evento);

        verify(ordenService).confirmarVentaDesdePago(evento);
    }

    @Test
    void ignoresNonApprovedPaymentEvents() {
        OrdenService ordenService = mock(OrdenService.class);
        ConsumidorPagoAprobado consumer = new ConsumidorPagoAprobado(ordenService);

        consumer.consumir(EventoPagoAprobado.builder().tipoEvento("pago.rechazado").build());

        verify(ordenService, never()).confirmarVentaDesdePago(org.mockito.ArgumentMatchers.any());
    }
}
