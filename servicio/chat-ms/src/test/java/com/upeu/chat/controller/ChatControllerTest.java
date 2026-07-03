package com.upeu.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.upeu.chat.dto.MensajeVentaValidadaRequest;
import com.upeu.chat.dto.MensajeVentaValidadaResponse;
import com.upeu.chat.service.ChatService;
import com.upeu.chat.service.MensajeService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ChatControllerTest {

    @Test
    void createsValidatedSaleMessage() {
        ChatService chatService = mock(ChatService.class);
        MensajeService mensajeService = mock(MensajeService.class);
        ChatController controller = new ChatController(chatService, mensajeService);
        MensajeVentaValidadaRequest request = MensajeVentaValidadaRequest.builder()
                .idComprador(1L)
                .idVendedor(4L)
                .publicacionId(10L)
                .idOrden(94L)
                .pagoId(38L)
                .tituloProducto("maquillaje para damas")
                .monto(BigDecimal.valueOf(80))
                .moneda("PEN")
                .mpPaymentId("166582439914")
                .build();
        when(chatService.crearMensajeVentaValidada(request)).thenReturn(
                new MensajeVentaValidadaResponse(15L, 120L, "Hola, compre tu producto", true)
        );

        ResponseEntity<MensajeVentaValidadaResponse> response = controller.createValidatedSaleMessage(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getChatId()).isEqualTo(15L);
        assertThat(response.getBody().getMensajeId()).isEqualTo(120L);
        assertThat(response.getBody().isCreado()).isTrue();
        verify(chatService).crearMensajeVentaValidada(request);
    }
}
