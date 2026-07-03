package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.client.ChatClient;
import com.upeu.pagos.client.OrdenClient;
import com.upeu.pagos.config.AppProperties;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.ActualizarEstadoOrdenRequest;
import com.upeu.pagos.dto.ComprobantePagoRequest;
import com.upeu.pagos.dto.ComprobantePagoResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.MensajeVentaValidadaRequest;
import com.upeu.pagos.dto.MensajeVentaValidadaResponse;
import com.upeu.pagos.dto.ValidarTransaccionMercadoPagoResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.evento.EventoPago;
import com.upeu.pagos.exception.ConflictException;
import com.upeu.pagos.exception.BadRequestException;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import com.upeu.pagos.service.ProductorPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MercadoPagoCheckoutServiceImplTest {

    @Test
    void createsPendingPagoAndStoresPreferenceId() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        MercadoPagoProperties properties = defaultProperties();
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            if (pago.getId() == null) {
                pago.setId(44L);
            }
            return pago;
        });
        when(client.createPreference(any(MercadoPagoPreferencePayload.class))).thenReturn(
                new MercadoPagoPreferenceResult("pref_44", "https://init", "https://sandbox")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago);

        MercadoPagoPreferenceResponse response = service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "YAPE",
                "comprador@ejemplo.com"
        ));

        assertThat(response.getPagoId()).isEqualTo(44L);
        assertThat(response.getIdOrden()).isEqualTo(12L);
        assertThat(response.getEstado()).isEqualTo("PENDIENTE");
        assertThat(response.getPreferenceId()).isEqualTo("pref_44");
        assertThat(response.getSandboxInitPoint()).isEqualTo("https://sandbox");
        assertThat(response.getCheckoutUrl()).isEqualTo("https://sandbox");
        assertThat(response.getExternalReference()).isEqualTo("ORDEN-12");
        verify(client).createPreference(any(MercadoPagoPreferencePayload.class));

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(repository, atLeastOnce()).save(pagoCaptor.capture());
        List<Pago> pagosGuardados = pagoCaptor.getAllValues();
        Pago pagoConSnapshot = pagosGuardados.get(pagosGuardados.size() - 1);
        assertThat(pagoConSnapshot.getIdVendedor()).isEqualTo(5L);
        assertThat(pagoConSnapshot.getPublicacionId()).isEqualTo(99L);
        assertThat(pagoConSnapshot.getTituloProducto()).isEqualTo("Audifonos");
        assertThat(pagoConSnapshot.getDescripcionProducto()).isEqualTo("Audifonos para clases");
        assertThat(pagoConSnapshot.getMoneda()).isEqualTo("PEN");
    }

    @Test
    void omitsAutoReturnByDefaultForLocalDevelopment() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        MercadoPagoProperties properties = defaultProperties();
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            if (pago.getId() == null) {
                pago.setId(44L);
            }
            return pago;
        });
        when(client.createPreference(any(MercadoPagoPreferencePayload.class))).thenReturn(
                new MercadoPagoPreferenceResult("pref_44", "https://init", "https://sandbox")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago);

        service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "MERCADO_PAGO",
                "comprador@ejemplo.com"
        ));

        ArgumentCaptor<MercadoPagoPreferencePayload> payloadCaptor = ArgumentCaptor.forClass(MercadoPagoPreferencePayload.class);
        verify(client).createPreference(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().autoReturn()).isNull();
    }

    @Test
    void createsBackUrlsFromFrontendUrlBeforeUsingAutoReturn() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        MercadoPagoProperties properties = defaultProperties();
        properties.setSuccessUrl(null);
        properties.setFailureUrl(null);
        properties.setPendingUrl(null);
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            if (pago.getId() == null) {
                pago.setId(44L);
            }
            return pago;
        });
        when(client.createPreference(any(MercadoPagoPreferencePayload.class))).thenReturn(
                new MercadoPagoPreferenceResult("pref_44", "https://init", "https://sandbox")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago);

        service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "MERCADO_PAGO",
                "comprador@ejemplo.com"
        ));

        ArgumentCaptor<MercadoPagoPreferencePayload> payloadCaptor = ArgumentCaptor.forClass(MercadoPagoPreferencePayload.class);
        verify(client).createPreference(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().backUrls().success()).isEqualTo("http://localhost:4200/pago/exito");
        assertThat(payloadCaptor.getValue().backUrls().failure()).isEqualTo("http://localhost:4200/pago/fallo");
        assertThat(payloadCaptor.getValue().backUrls().pending()).isEqualTo("http://localhost:4200/pago/pendiente");
        assertThat(payloadCaptor.getValue().autoReturn()).isNull();
    }

    @Test
    void throwsClearErrorWhenFrontendUrlIsBlank() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        AppProperties appProperties = new AppProperties();
        appProperties.setFrontendUrl(" ");
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, defaultProperties(), appProperties, new PagoMapper(), ordenClient, chatClient, productorPago);

        assertThatThrownBy(() -> service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "MERCADO_PAGO",
                "comprador@ejemplo.com"
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessage("frontendUrl no configurado para Mercado Pago");
    }

    @Test
    void usesAutoReturnApprovedWhenSuccessUrlIsHttps() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        MercadoPagoProperties properties = defaultProperties();
        properties.setAutoReturnEnabled(true);
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            if (pago.getId() == null) {
                pago.setId(44L);
            }
            return pago;
        });
        when(client.createPreference(any(MercadoPagoPreferencePayload.class))).thenReturn(
                new MercadoPagoPreferenceResult("pref_44", "https://init", "https://sandbox")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago);

        service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "MERCADO_PAGO",
                "comprador@ejemplo.com"
        ));

        ArgumentCaptor<MercadoPagoPreferencePayload> payloadCaptor = ArgumentCaptor.forClass(MercadoPagoPreferencePayload.class);
        verify(client).createPreference(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().autoReturn()).isEqualTo("approved");
    }

    @Test
    void webhookPaymentApprovedUpdatesPagoStatus() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        MercadoPagoProperties properties = defaultProperties();
        Pago pago = Pago.builder()
                .id(44L)
                .idOrden(12L)
                .idComprador(8L)
                .monto(BigDecimal.valueOf(70))
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .externalReference("ORDEN-12")
                .build();
        when(client.getPayment("9988")).thenReturn(
                new MercadoPagoPaymentResult("9988", "approved", "ORDEN-12")
        );
        when(repository.findByExternalReference("ORDEN-12")).thenReturn(Optional.of(pago));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago);

        service.syncPayment("9988");

        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(pago.getMpPaymentId()).isEqualTo("9988");
        assertThat(pago.getMpStatus()).isEqualTo("approved");
        verify(repository).save(pago);
    }

    @Test
    void confirmedApprovedPaymentPublishesApprovedPaymentEventWithoutCallingChat() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(44L)
                .idOrden(47L)
                .idComprador(2L)
                .idVendedor(5L)
                .publicacionId(7L)
                .tituloProducto("polera")
                .descripcionProducto("polera talla L")
                .monto(BigDecimal.valueOf(100))
                .moneda("PEN")
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .externalReference("ORDEN-47")
                .build();
        when(client.getPayment("123456789")).thenReturn(
                new MercadoPagoPaymentResult("123456789", "approved", "ORDEN-47")
        );
        when(repository.findByExternalReference("ORDEN-47")).thenReturn(Optional.of(pago));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository,
                client,
                defaultProperties(),
                defaultAppProperties(),
                new PagoMapper(),
                ordenClient,
                chatClient,
                productorPago
        );

        PagoConfirmacionResponse response = service.confirmarPago("123456789", "approved", "ORDEN-47");

        assertThat(response.getPagoId()).isEqualTo(44L);
        assertThat(response.getOrdenId()).isEqualTo(47L);
        assertThat(response.getEstado()).isEqualTo("APROBADO");
        assertThat(response.getEstadoPago()).isEqualTo("APROBADO");
        assertThat(response.getEstadoOrden()).isEqualTo("PAGADA");
        assertThat(response.getChatId()).isNull();
        assertThat(response.getConversacionId()).isNull();
        assertThat(response.getMensaje()).contains("evento de pago aprobado");
        assertThat(response.isMensajeComprobanteEnviado()).isFalse();
        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(pago.getMpPaymentId()).isEqualTo("123456789");

        ArgumentCaptor<EventoPago> eventoCaptor = ArgumentCaptor.forClass(EventoPago.class);
        verify(productorPago).enviarEventoPago(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getEventId()).isNotBlank();
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo("pago.aprobado");
        assertThat(eventoCaptor.getValue().getEventType()).isEqualTo("pago.aprobado");
        assertThat(eventoCaptor.getValue().getPagoId()).isEqualTo(44L);
        assertThat(eventoCaptor.getValue().getOrdenId()).isEqualTo(47L);
        assertThat(eventoCaptor.getValue().getIdComprador()).isEqualTo(2L);
        assertThat(eventoCaptor.getValue().getCompradorId()).isEqualTo(2L);
        assertThat(eventoCaptor.getValue().getIdVendedor()).isEqualTo(5L);
        assertThat(eventoCaptor.getValue().getVendedorId()).isEqualTo(5L);
        assertThat(eventoCaptor.getValue().getProductoId()).isEqualTo(7L);
        assertThat(eventoCaptor.getValue().getPublicacionId()).isEqualTo(7L);
        assertThat(eventoCaptor.getValue().getTituloProducto()).isEqualTo("polera");
        assertThat(eventoCaptor.getValue().getEstadoPago()).isEqualTo("APROBADO");
        verify(ordenClient, never()).updateEstado(any(), any());
        verify(chatClient, never()).createReceipt(any(ComprobantePagoRequest.class));
    }

    @Test
    void alreadyApprovedPaymentReturnsStoredChatIdWithoutSendingDuplicateReceipt() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(44L)
                .idOrden(47L)
                .idComprador(2L)
                .idVendedor(5L)
                .estado("APROBADO")
                .externalReference("ORDEN-47")
                .mpPaymentId("123456789")
                .chatId(10L)
                .eventoPagoAprobadoPublicado(true)
                .build();
        when(client.getPayment("123456789")).thenReturn(
                new MercadoPagoPaymentResult("123456789", "approved", "ORDEN-47")
        );
        when(repository.findByExternalReference("ORDEN-47")).thenReturn(Optional.of(pago));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository,
                client,
                defaultProperties(),
                defaultAppProperties(),
                new PagoMapper(),
                ordenClient,
                chatClient,
                productorPago
        );

        PagoConfirmacionResponse response = service.confirmarPago("123456789", "approved", "ORDEN-47");

        assertThat(response.getChatId()).isEqualTo(10L);
        assertThat(response.getConversacionId()).isEqualTo(10L);
        assertThat(response.getMensaje()).contains("previamente");
        verify(productorPago, never()).enviarEventoPago(any(EventoPago.class));
        verify(chatClient, never()).createReceipt(any(ComprobantePagoRequest.class));
    }

    @Test
    void validarTransaccionApprovedUpdatesPagoAndPublishesEventOnce() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(25L)
                .idOrden(83L)
                .idComprador(8L)
                .idVendedor(2L)
                .publicacionId(77L)
                .tituloProducto("polera")
                .monto(BigDecimal.valueOf(100).setScale(2))
                .moneda("PEN")
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .externalReference("ORDEN-83")
                .mpPreferenceId("pref_83")
                .build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        when(repository.findByMpPaymentId("166617913516")).thenReturn(Optional.empty());
        when(client.obtenerPagoPorId("166617913516")).thenReturn(
                new MercadoPagoPaymentResult("166617913516", "approved", "ORDEN-83", BigDecimal.valueOf(100).setScale(2), "PEN")
        );
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago
        );

        ValidarTransaccionMercadoPagoResponse response = service.validarTransaccion(25L, " 166617913516 ");

        assertThat(response.getPagoId()).isEqualTo(25L);
        assertThat(response.getIdOrden()).isEqualTo(83L);
        assertThat(response.getEstado()).isEqualTo("APROBADO");
        assertThat(response.getMercadoPagoPaymentId()).isEqualTo("166617913516");
        assertThat(response.getMpPaymentId()).isEqualTo("166617913516");
        assertThat(response.getPreferenceId()).isEqualTo("pref_83");
        assertThat(response.getExternalReference()).isEqualTo("ORDEN-83");
        assertThat(response.getTituloProducto()).isEqualTo("polera");
        assertThat(response.getMonto()).isEqualByComparingTo("100.00");
        assertThat(response.getMoneda()).isEqualTo("PEN");
        assertThat(response.getMensaje()).contains("numero de venta #83");
        assertThat(response.getMensaje()).contains("S/ 100.00");
        assertThat(response.getChatMessageCreated()).isFalse();
        assertThat(pago.getMpPaymentId()).isEqualTo("166617913516");
        assertThat(pago.getMpStatus()).isEqualTo("approved");
        assertThat(pago.getFechaConfirmacion()).isNotNull();
        assertThat(pago.isEventoPagoAprobadoPublicado()).isTrue();
        ArgumentCaptor<EventoPago> eventoCaptor = ArgumentCaptor.forClass(EventoPago.class);
        verify(productorPago).enviarEventoPago(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getPagoId()).isEqualTo(25L);
        assertThat(eventoCaptor.getValue().getEventId()).isNotBlank();
        assertThat(eventoCaptor.getValue().getEventType()).isEqualTo("pago.aprobado");
        assertThat(eventoCaptor.getValue().getOrdenId()).isEqualTo(83L);
        assertThat(eventoCaptor.getValue().getIdComprador()).isEqualTo(8L);
        assertThat(eventoCaptor.getValue().getCompradorId()).isEqualTo(8L);
        assertThat(eventoCaptor.getValue().getIdVendedor()).isEqualTo(2L);
        assertThat(eventoCaptor.getValue().getVendedorId()).isEqualTo(2L);
        assertThat(eventoCaptor.getValue().getProductoId()).isEqualTo(77L);
        assertThat(eventoCaptor.getValue().getPublicacionId()).isEqualTo(77L);
        assertThat(eventoCaptor.getValue().getTituloProducto()).isEqualTo("polera");
        assertThat(eventoCaptor.getValue().getMoneda()).isEqualTo("PEN");
        assertThat(eventoCaptor.getValue().getMpPaymentId()).isEqualTo("166617913516");
        verify(chatClient, never()).createValidatedSaleMessage(any(MensajeVentaValidadaRequest.class));
    }

    @Test
    void validarTransaccionApprovedKeepsPagoApprovedWithoutDependingOnChat() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(25L)
                .idOrden(83L)
                .idComprador(8L)
                .idVendedor(2L)
                .publicacionId(77L)
                .tituloProducto("polera")
                .monto(BigDecimal.valueOf(100).setScale(2))
                .moneda("PEN")
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .externalReference("ORDEN-83")
                .build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        when(repository.findByMpPaymentId("166617913516")).thenReturn(Optional.empty());
        when(client.obtenerPagoPorId("166617913516")).thenReturn(
                new MercadoPagoPaymentResult("166617913516", "approved", "ORDEN-83", BigDecimal.valueOf(100).setScale(2), "PEN")
        );
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), ordenClient, chatClient, productorPago
        );

        ValidarTransaccionMercadoPagoResponse response = service.validarTransaccion(25L, "166617913516");

        assertThat(response.getEstado()).isEqualTo("APROBADO");
        assertThat(response.getChatMessageCreated()).isFalse();
        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        verify(productorPago).enviarEventoPago(any(EventoPago.class));
        verify(chatClient, never()).createValidatedSaleMessage(any(MensajeVentaValidadaRequest.class));
    }

    @Test
    void validarTransaccionAlreadyPublishedDoesNotDuplicateEvent() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(25L)
                .idOrden(83L)
                .idComprador(8L)
                .tituloProducto("polera")
                .monto(BigDecimal.valueOf(100).setScale(2))
                .moneda("PEN")
                .metodoPago("MERCADO_PAGO")
                .estado("APROBADO")
                .externalReference("ORDEN-83")
                .mpPaymentId("166617913516")
                .mpPreferenceId("pref_83")
                .eventoPagoAprobadoPublicado(true)
                .build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), mock(OrdenClient.class), mock(ChatClient.class), productorPago
        );

        ValidarTransaccionMercadoPagoResponse response = service.validarTransaccion(25L, "166617913516");

        assertThat(response.getEstado()).isEqualTo("APROBADO");
        assertThat(response.getTituloProducto()).isEqualTo("polera");
        assertThat(response.getMensaje()).contains("polera");
        verify(productorPago, never()).enviarEventoPago(any());
        verify(client, never()).obtenerPagoPorId(any());
    }

    @Test
    void validarTransaccionRejectsPaymentIdUsedByAnotherPago() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder().id(25L).idOrden(83L).estado("PENDIENTE").externalReference("ORDEN-83").build();
        Pago other = Pago.builder().id(26L).idOrden(84L).mpPaymentId("166617913516").build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        when(repository.findByMpPaymentId("166617913516")).thenReturn(Optional.of(other));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), mock(OrdenClient.class), mock(ChatClient.class), productorPago
        );

        assertThatThrownBy(() -> service.validarTransaccion(25L, "166617913516"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya fue usado");
        verify(client, never()).obtenerPagoPorId(any());
    }

    @Test
    void validarTransaccionRejectsDifferentExternalReference() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(25L)
                .idOrden(83L)
                .monto(BigDecimal.valueOf(100))
                .moneda("PEN")
                .estado("PENDIENTE")
                .externalReference("ORDEN-83")
                .build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        when(repository.findByMpPaymentId("166617913516")).thenReturn(Optional.empty());
        when(client.obtenerPagoPorId("166617913516")).thenReturn(
                new MercadoPagoPaymentResult("166617913516", "approved", "ORDEN-999", BigDecimal.valueOf(100), "PEN")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), mock(OrdenClient.class), mock(ChatClient.class), productorPago
        );

        assertThatThrownBy(() -> service.validarTransaccion(25L, "166617913516"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no corresponde");
    }

    @Test
    void validarTransaccionRejectsDifferentAmount() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        Pago pago = Pago.builder()
                .id(25L)
                .idOrden(83L)
                .monto(BigDecimal.valueOf(100))
                .moneda("PEN")
                .estado("PENDIENTE")
                .externalReference("ORDEN-83")
                .build();
        when(repository.findById(25L)).thenReturn(Optional.of(pago));
        when(repository.findByMpPaymentId("166617913516")).thenReturn(Optional.empty());
        when(client.obtenerPagoPorId("166617913516")).thenReturn(
                new MercadoPagoPaymentResult("166617913516", "approved", "ORDEN-83", BigDecimal.valueOf(90), "PEN")
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository, client, defaultProperties(), defaultAppProperties(), new PagoMapper(), mock(OrdenClient.class), mock(ChatClient.class), productorPago
        );

        assertThatThrownBy(() -> service.validarTransaccion(25L, "166617913516"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("monto");
    }

    private MercadoPagoProperties defaultProperties() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("dummy-token");
        properties.setBaseUrl("https://api.mercadopago.com");
        properties.setNotificationUrl("http://localhost:18080/api/v1/pagos/mercadopago/webhook");
        properties.setSuccessUrl("http://localhost:4200/pago/exito");
        properties.setFailureUrl("http://localhost:4200/pago/fallo");
        properties.setPendingUrl("http://localhost:4200/pago/pendiente");
        return properties;
    }

    private AppProperties defaultAppProperties() {
        AppProperties properties = new AppProperties();
        properties.setFrontendUrl("http://localhost:4200");
        return properties;
    }
}
