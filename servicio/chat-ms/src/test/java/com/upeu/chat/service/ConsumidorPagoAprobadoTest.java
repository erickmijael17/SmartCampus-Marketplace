package com.upeu.chat.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.upeu.chat.dto.EventoPago;
import org.junit.jupiter.api.Test;

class ConsumidorPagoAprobadoTest {

    @Test
    void consumesApprovedPaymentEvent() {
        ChatService chatService = mock(ChatService.class);
        ConsumidorPagoAprobado consumer = new ConsumidorPagoAprobado(chatService);
        EventoPago evento = EventoPago.builder()
                .tipoEvento("pago.aprobado")
                .ordenId(94L)
                .idComprador(1L)
                .idVendedor(4L)
                .publicacionId(8L)
                .build();

        consumer.consumir(evento);

        verify(chatService).crearComprobantePagoAprobado(evento);
    }

    @Test
    void ignoresNonApprovedPaymentEvents() {
        ChatService chatService = mock(ChatService.class);
        ConsumidorPagoAprobado consumer = new ConsumidorPagoAprobado(chatService);

        consumer.consumir(EventoPago.builder().tipoEvento("pago.rechazado").build());

        verify(chatService, never()).crearComprobantePagoAprobado(org.mockito.ArgumentMatchers.any());
    }
}
