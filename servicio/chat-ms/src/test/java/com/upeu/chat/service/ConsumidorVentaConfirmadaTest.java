package com.upeu.chat.service;

import com.upeu.chat.dto.EventoVentaConfirmada;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;

class ConsumidorVentaConfirmadaTest {

    @Test
    void consumesConfirmedSaleEvent() {
        ChatService chatService = mock(ChatService.class);
        ConsumidorVentaConfirmada consumer = new ConsumidorVentaConfirmada(chatService);
        EventoVentaConfirmada evento = EventoVentaConfirmada.builder()
                .eventType("venta.confirmada")
                .ordenId(83L)
                .pagoId(25L)
                .compradorId(8L)
                .vendedorId(2L)
                .productoId(77L)
                .build();

        consumer.consumir(evento);

        verify(chatService).crearMensajesVentaConfirmada(evento);
    }

    @Test
    void ignoresDifferentEventTypes() {
        ChatService chatService = mock(ChatService.class);
        ConsumidorVentaConfirmada consumer = new ConsumidorVentaConfirmada(chatService);

        consumer.consumir(EventoVentaConfirmada.builder().eventType("orden.creada").build());

        verify(chatService, never()).crearMensajesVentaConfirmada(org.mockito.ArgumentMatchers.any());
    }
}
