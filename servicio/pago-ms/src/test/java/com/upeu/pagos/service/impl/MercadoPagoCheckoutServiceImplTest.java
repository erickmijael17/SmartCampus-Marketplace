package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MercadoPagoCheckoutServiceImplTest {

    @Test
    void createsPendingPagoAndStoresPreferenceId() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
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
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper());

        MercadoPagoPreferenceResponse response = service.createPreference(new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                "Audifonos",
                "Audifonos para clases",
                2,
                BigDecimal.valueOf(35),
                "YAPE"
        ));

        assertThat(response.getPagoId()).isEqualTo(44L);
        assertThat(response.getIdOrden()).isEqualTo(12L);
        assertThat(response.getEstado()).isEqualTo("PENDIENTE");
        assertThat(response.getPreferenceId()).isEqualTo("pref_44");
        assertThat(response.getSandboxInitPoint()).isEqualTo("https://sandbox");
        verify(client).createPreference(any(MercadoPagoPreferencePayload.class));
    }

    @Test
    void webhookPaymentApprovedUpdatesPagoStatus() {
        PagoRepository repository = mock(PagoRepository.class);
        MercadoPagoClient client = mock(MercadoPagoClient.class);
        MercadoPagoProperties properties = defaultProperties();
        Pago pago = Pago.builder()
                .id(44L)
                .idOrden(12L)
                .idComprador(8L)
                .monto(BigDecimal.valueOf(70))
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .externalReference("SCM-ORDEN-12-PAGO-44")
                .build();
        when(client.getPayment("9988")).thenReturn(
                new MercadoPagoPaymentResult("9988", "approved", "SCM-ORDEN-12-PAGO-44")
        );
        when(repository.findByExternalReference("SCM-ORDEN-12-PAGO-44")).thenReturn(Optional.of(pago));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MercadoPagoCheckoutServiceImpl service = new MercadoPagoCheckoutServiceImpl(repository, client, properties, new PagoMapper());

        service.syncPayment("9988");

        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(pago.getMpPaymentId()).isEqualTo("9988");
        assertThat(pago.getMpStatus()).isEqualTo("approved");
        verify(repository).save(pago);
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
