package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.client.ChatClient;
import com.upeu.pagos.client.OrdenClient;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.ActualizarEstadoOrdenRequest;
import com.upeu.pagos.dto.ComprobantePagoRequest;
import com.upeu.pagos.dto.ComprobantePagoResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper(), ordenClient, chatClient);

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
    void omitsAutoReturnWhenSuccessUrlIsLocalhost() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
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
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper(), ordenClient, chatClient);

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
    void usesAutoReturnApprovedWhenSuccessUrlIsHttps() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
        MercadoPagoProperties properties = defaultProperties();
        properties.setSuccessUrl("https://smartcampus.test/pago/exito");
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
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper(), ordenClient, chatClient);

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
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper(), ordenClient, chatClient);

        service.syncPayment("9988");

        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(pago.getMpPaymentId()).isEqualTo("9988");
        assertThat(pago.getMpStatus()).isEqualTo("approved");
        verify(repository).save(pago);
    }

    @Test
    void confirmedApprovedPaymentUpdatesOrderAndCreatesChatReceipt() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        OrdenClient ordenClient = mock(OrdenClient.class);
        ChatClient chatClient = mock(ChatClient.class);
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
        when(ordenClient.updateEstado(any(), any())).thenReturn(new Object());
        when(chatClient.createReceipt(any(ComprobantePagoRequest.class))).thenReturn(
                new ComprobantePagoResponse(10L, 99L, true)
        );
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(
                repository,
                client,
                defaultProperties(),
                new PagoMapper(),
                ordenClient,
                chatClient
        );

        PagoConfirmacionResponse response = service.confirmarPago("123456789");

        assertThat(response.getOrdenId()).isEqualTo(47L);
        assertThat(response.getEstadoPago()).isEqualTo("APROBADO");
        assertThat(response.getEstadoOrden()).isEqualTo("PAGADA");
        assertThat(response.getChatId()).isEqualTo(10L);
        assertThat(response.isMensajeComprobanteEnviado()).isTrue();
        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(pago.getMpPaymentId()).isEqualTo("123456789");

        ArgumentCaptor<ComprobantePagoRequest> comprobanteCaptor = ArgumentCaptor.forClass(ComprobantePagoRequest.class);
        verify(chatClient).createReceipt(comprobanteCaptor.capture());
        assertThat(comprobanteCaptor.getValue().getIdComprador()).isEqualTo(2L);
        assertThat(comprobanteCaptor.getValue().getIdVendedor()).isEqualTo(5L);
        assertThat(comprobanteCaptor.getValue().getTituloProducto()).isEqualTo("polera");
    }

    private MercadoPagoProperties defaultProperties() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("dummy-token");
        properties.setBaseUrl("https://api.mercadopago.com");
        properties.setNotificationUrl("http://localhost:18080/api/v1/pagos/mercadopago/webhook");
        properties.setSuccessUrl("http://localhost:4200/payment-result?status=success");
        properties.setFailureUrl("http://localhost:4200/payment-result?status=failure");
        properties.setPendingUrl("http://localhost:4200/payment-result?status=pending");
        return properties;
    }
}
