package com.upeu.chat.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.entity.Conversacion;
import com.upeu.chat.entity.Mensaje;
import com.upeu.chat.repository.ConversacionRepository;
import com.upeu.chat.repository.MensajeRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChatServiceImplTest {

    @Test
    void createsAutomaticReceiptMessageReusingBuyerPublisherConversation() {
        ConversacionRepository conversacionRepository = mock(ConversacionRepository.class);
        MensajeRepository mensajeRepository = mock(MensajeRepository.class);
        ChatServiceImpl service = new ChatServiceImpl(conversacionRepository, mensajeRepository);
        when(conversacionRepository.findBetweenUsers(2L, 5L)).thenReturn(Optional.of(
                Conversacion.builder().id(10L).idUsuario1(2L).idUsuario2(5L).build()
        ));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> {
            Mensaje mensaje = invocation.getArgument(0);
            mensaje.setId(99L);
            return mensaje;
        });

        ComprobantePagoResponse response = service.crearComprobante(ComprobantePagoRequest.builder()
                .idComprador(2L)
                .idVendedor(5L)
                .ordenId(47L)
                .tituloProducto("polera")
                .monto(BigDecimal.valueOf(100))
                .moneda("PEN")
                .estadoPago("APROBADO")
                .metodoPago("Mercado Pago")
                .paymentId("123456789")
                .build());

        assertThat(response.getChatId()).isEqualTo(10L);
        assertThat(response.getMensajeId()).isEqualTo(99L);
        assertThat(response.isMensajeComprobanteEnviado()).isTrue();

        ArgumentCaptor<Mensaje> mensajeCaptor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(mensajeCaptor.capture());
        assertThat(mensajeCaptor.getValue().getIdConversacion()).isEqualTo(10L);
        assertThat(mensajeCaptor.getValue().getIdRemitente()).isNull();
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Pago confirmado");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Producto: polera");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Orden: #47");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Referencia: 123456789");
    }
}
