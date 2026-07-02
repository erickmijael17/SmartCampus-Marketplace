package com.upeu.chat.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.dto.EventoPago;
import com.upeu.chat.dto.MensajeVentaValidadaRequest;
import com.upeu.chat.dto.MensajeVentaValidadaResponse;
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
        when(conversacionRepository.findByIdOrden(47L)).thenReturn(Optional.empty());
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
        assertThat(mensajeCaptor.getValue().getIdOrden()).isEqualTo(47L);
        assertThat(mensajeCaptor.getValue().getMpPaymentId()).isEqualTo("123456789");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Pago confirmado");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Producto: polera");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Orden: #47");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Referencia: 123456789");
    }

    @Test
    void createsSaleChatAndSystemMessageFromApprovedPaymentEvent() {
        ConversacionRepository conversacionRepository = mock(ConversacionRepository.class);
        MensajeRepository mensajeRepository = mock(MensajeRepository.class);
        ChatServiceImpl service = new ChatServiceImpl(conversacionRepository, mensajeRepository);
        when(conversacionRepository.findByPublicacionAndUsers(8L, 1L, 2L)).thenReturn(Optional.empty());
        when(conversacionRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(conversacionRepository.findByIdOrden(91L)).thenReturn(Optional.empty());
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(invocation -> {
            Conversacion conversacion = invocation.getArgument(0);
            conversacion.setId(15L);
            return conversacion;
        });
        when(mensajeRepository.existsByIdConversacionAndIdOrdenAndTipoMensaje(15L, 91L, "SISTEMA_CONFIRMACION_PAGO")).thenReturn(false);
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> {
            Mensaje mensaje = invocation.getArgument(0);
            mensaje.setId(77L);
            return mensaje;
        });

        ComprobantePagoResponse response = service.crearComprobantePagoAprobado(EventoPago.builder()
                .tipoEvento("pago.aprobado")
                .pagoId(30L)
                .ordenId(91L)
                .idComprador(1L)
                .nombreComprador("admin")
                .idVendedor(2L)
                .publicacionId(8L)
                .tituloProducto("Bicicleta")
                .monto(300.0)
                .moneda("PEN")
                .mpPaymentId("166640293542")
                .build());

        assertThat(response.getChatId()).isEqualTo(15L);
        assertThat(response.isMensajeComprobanteEnviado()).isTrue();

        ArgumentCaptor<Conversacion> conversacionCaptor = ArgumentCaptor.forClass(Conversacion.class);
        verify(conversacionRepository).save(conversacionCaptor.capture());
        assertThat(conversacionCaptor.getValue().getIdUsuario1()).isEqualTo(1L);
        assertThat(conversacionCaptor.getValue().getIdUsuario2()).isEqualTo(2L);
        assertThat(conversacionCaptor.getValue().getPublicacionId()).isEqualTo(8L);
        assertThat(conversacionCaptor.getValue().getIdOrden()).isEqualTo(91L);
        assertThat(conversacionCaptor.getValue().getTipoChat()).isEqualTo("VENTA");

        ArgumentCaptor<Mensaje> mensajeCaptor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(mensajeCaptor.capture());
        assertThat(mensajeCaptor.getValue().getTipoMensaje()).isEqualTo("SISTEMA_CONFIRMACION_PAGO");
        assertThat(mensajeCaptor.getValue().getTipoRemitente()).isEqualTo("SISTEMA");
        assertThat(mensajeCaptor.getValue().getIdOrden()).isEqualTo(91L);
        assertThat(mensajeCaptor.getValue().getPagoId()).isEqualTo(30L);
        assertThat(mensajeCaptor.getValue().getMpPaymentId()).isEqualTo("166640293542");
        assertThat(mensajeCaptor.getValue().getIdRemitente()).isNull();
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Compra confirmada");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("admin");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("Bicicleta");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("#91");
        assertThat(mensajeCaptor.getValue().getContenido()).contains("S/ 300.00");
    }

    @Test
    void approvedPaymentEventIsIdempotentForSameOrder() {
        ConversacionRepository conversacionRepository = mock(ConversacionRepository.class);
        MensajeRepository mensajeRepository = mock(MensajeRepository.class);
        ChatServiceImpl service = new ChatServiceImpl(conversacionRepository, mensajeRepository);
        Conversacion existing = Conversacion.builder()
                .id(15L)
                .idUsuario1(1L)
                .idUsuario2(2L)
                .publicacionId(8L)
                .idOrden(91L)
                .tipoChat("VENTA")
                .build();
        when(conversacionRepository.findByPublicacionAndUsers(8L, 1L, 2L)).thenReturn(Optional.of(existing));
        when(mensajeRepository.existsByIdConversacionAndIdOrdenAndTipoMensaje(15L, 91L, "SISTEMA_CONFIRMACION_PAGO")).thenReturn(true);

        ComprobantePagoResponse response = service.crearComprobantePagoAprobado(EventoPago.builder()
                .tipoEvento("pago.aprobado")
                .ordenId(91L)
                .idComprador(1L)
                .idVendedor(2L)
                .publicacionId(8L)
                .tituloProducto("Bicicleta")
                .monto(300.0)
                .moneda("PEN")
                .mpPaymentId("166640293542")
                .build());

        assertThat(response.getChatId()).isEqualTo(15L);
        assertThat(response.isMensajeComprobanteEnviado()).isFalse();
        verify(conversacionRepository, never()).save(any(Conversacion.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @Test
    void validatedSaleMessageCreatesChatAndMessageFromBuyer() {
        ConversacionRepository conversacionRepository = mock(ConversacionRepository.class);
        MensajeRepository mensajeRepository = mock(MensajeRepository.class);
        ChatServiceImpl service = new ChatServiceImpl(conversacionRepository, mensajeRepository);
        when(conversacionRepository.findByPublicacionAndUsers(10L, 1L, 4L)).thenReturn(Optional.empty());
        when(conversacionRepository.findBetweenUsers(1L, 4L)).thenReturn(Optional.empty());
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(invocation -> {
            Conversacion conversacion = invocation.getArgument(0);
            conversacion.setId(15L);
            return conversacion;
        });
        when(mensajeRepository.findFirstByIdConversacionAndIdOrdenAndPagoIdAndMpPaymentIdAndTipoMensaje(
                15L, 94L, 38L, "166582439914", "VENTA_VALIDADA"
        )).thenReturn(Optional.empty());
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> {
            Mensaje mensaje = invocation.getArgument(0);
            mensaje.setId(120L);
            return mensaje;
        });

        MensajeVentaValidadaResponse response = service.crearMensajeVentaValidada(MensajeVentaValidadaRequest.builder()
                .idComprador(1L)
                .idVendedor(4L)
                .publicacionId(10L)
                .idOrden(94L)
                .pagoId(38L)
                .tituloProducto("maquillaje para damas")
                .monto(BigDecimal.valueOf(80))
                .moneda("PEN")
                .mpPaymentId("166582439914")
                .build());

        assertThat(response.getChatId()).isEqualTo(15L);
        assertThat(response.getMensajeId()).isEqualTo(120L);
        assertThat(response.isCreado()).isTrue();
        assertThat(response.getMensaje()).contains("Hola, compr\u00e9 tu producto");
        assertThat(response.getMensaje()).contains("maquillaje para damas");

        ArgumentCaptor<Mensaje> mensajeCaptor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(mensajeCaptor.capture());
        assertThat(mensajeCaptor.getValue().getIdConversacion()).isEqualTo(15L);
        assertThat(mensajeCaptor.getValue().getIdRemitente()).isEqualTo(1L);
        assertThat(mensajeCaptor.getValue().getTipoRemitente()).isEqualTo("USUARIO");
        assertThat(mensajeCaptor.getValue().getTipoMensaje()).isEqualTo("VENTA_VALIDADA");
        assertThat(mensajeCaptor.getValue().getIdOrden()).isEqualTo(94L);
        assertThat(mensajeCaptor.getValue().getPagoId()).isEqualTo(38L);
        assertThat(mensajeCaptor.getValue().getMpPaymentId()).isEqualTo("166582439914");
    }

    @Test
    void validatedSaleMessageReusesExistingChatAndAvoidsDuplicates() {
        ConversacionRepository conversacionRepository = mock(ConversacionRepository.class);
        MensajeRepository mensajeRepository = mock(MensajeRepository.class);
        ChatServiceImpl service = new ChatServiceImpl(conversacionRepository, mensajeRepository);
        Conversacion existing = Conversacion.builder()
                .id(15L)
                .idUsuario1(1L)
                .idUsuario2(4L)
                .publicacionId(10L)
                .build();
        Mensaje existingMessage = Mensaje.builder()
                .id(120L)
                .idConversacion(15L)
                .idRemitente(1L)
                .tipoMensaje("VENTA_VALIDADA")
                .build();
        when(conversacionRepository.findByPublicacionAndUsers(10L, 1L, 4L)).thenReturn(Optional.of(existing));
        when(mensajeRepository.findFirstByIdConversacionAndIdOrdenAndPagoIdAndMpPaymentIdAndTipoMensaje(
                15L, 94L, 38L, "166582439914", "VENTA_VALIDADA"
        )).thenReturn(Optional.of(existingMessage));

        MensajeVentaValidadaResponse response = service.crearMensajeVentaValidada(MensajeVentaValidadaRequest.builder()
                .idComprador(1L)
                .idVendedor(4L)
                .publicacionId(10L)
                .idOrden(94L)
                .pagoId(38L)
                .tituloProducto("maquillaje para damas")
                .monto(BigDecimal.valueOf(80))
                .moneda("PEN")
                .mpPaymentId("166582439914")
                .build());

        assertThat(response.getChatId()).isEqualTo(15L);
        assertThat(response.getMensajeId()).isEqualTo(120L);
        assertThat(response.isCreado()).isFalse();
        verify(conversacionRepository, never()).save(any(Conversacion.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }
}
