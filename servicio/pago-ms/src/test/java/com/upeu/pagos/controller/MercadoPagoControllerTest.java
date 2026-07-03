package com.upeu.pagos.controller;

import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.service.MercadoPagoCheckoutService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MercadoPagoControllerTest {

    @Test
    void createPreferenceReturnsSandboxInitPoint() {
        MercadoPagoCheckoutService service = mock(MercadoPagoCheckoutService.class);
        MercadoPagoController controller = new MercadoPagoController(service);
        MercadoPagoPreferenceRequest request = new MercadoPagoPreferenceRequest(
                12L,
                8L,
                99L,
                5L,
                "Audifonos",
                "Audifonos para clases",
                1,
                BigDecimal.valueOf(50),
                "TARJETA",
                "comprador@ejemplo.com"
        );
        when(service.createPreference(request)).thenReturn(new MercadoPagoPreferenceResponse(
                44L,
                12L,
                "PENDIENTE",
                "pref_44",
                "https://init",
                "https://sandbox",
                "ORDEN-12"
        ));

        ResponseEntity<?> response = controller.createPreference(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        MercadoPagoPreferenceResponse body = (MercadoPagoPreferenceResponse) response.getBody();
        assertThat(body.getSandboxInitPoint()).isEqualTo("https://sandbox");
        assertThat(body.getExternalReference()).isEqualTo("ORDEN-12");
    }

    @Test
    void webhookSyncsPaymentIdFromBodyData() {
        MercadoPagoCheckoutService service = mock(MercadoPagoCheckoutService.class);
        MercadoPagoController controller = new MercadoPagoController(service);

        ResponseEntity<Void> response = controller.webhook(Map.of("data", Map.of("id", "9988")), Map.of());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(service).confirmarPago("9988", null, null);
    }

    @Test
    void confirmarPagoUsesCollectionIdAndExternalReferenceWhenPaymentIdIsMissing() {
        MercadoPagoCheckoutService service = mock(MercadoPagoCheckoutService.class);
        MercadoPagoController controller = new MercadoPagoController(service);

        ResponseEntity<?> response = controller.confirmarPago(Map.of(
                "collection_id", "7788",
                "status", "approved",
                "external_reference", "ORDEN-12"
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(service).confirmarPago("7788", "approved", "ORDEN-12");
    }
}
