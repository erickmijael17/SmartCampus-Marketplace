package com.upeu.pagos.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upeu.pagos.config.MercadoPagoProperties;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoClientTest {

    @Test
    void preferencePayloadSerializesMercadoPagoFieldNames() throws Exception {
        MercadoPagoPreferencePayload payload = samplePayload();
        String json = new ObjectMapper().writeValueAsString(payload);

        assertThat(json).contains("\"back_urls\"");
        assertThat(json).contains("\"success\":\"http://localhost:4200/pago/exito\"");
        assertThat(json).contains("\"failure\":\"http://localhost:4200/pago/fallo\"");
        assertThat(json).contains("\"pending\":\"http://localhost:4200/pago/pendiente\"");
        assertThat(json).contains("\"auto_return\":\"approved\"");
        assertThat(json).contains("\"external_reference\":\"ORDEN-1\"");
        assertThat(json).doesNotContain("backUrls");
        assertThat(json).doesNotContain("autoReturn");
        assertThat(json).doesNotContain("externalReference");
    }

    @Test
    void preferencePayloadOmitsAutoReturnWhenDisabled() throws Exception {
        MercadoPagoPreferencePayload payload = new MercadoPagoPreferencePayload(
                samplePayload().items(),
                samplePayload().backUrls(),
                samplePayload().notificationUrl(),
                samplePayload().externalReference(),
                null
        );

        String json = new ObjectMapper().writeValueAsString(payload);

        assertThat(json).contains("\"back_urls\"");
        assertThat(json).contains("\"success\":\"http://localhost:4200/pago/exito\"");
        assertThat(json).doesNotContain("auto_return");
        assertThat(json).doesNotContain("autoReturn");
    }

    @Test
    void createPreferenceSendsBearerAccessToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("dummy-token");
        properties.setBaseUrl("https://api.mercadopago.com");
        MercadoPagoClient client = new MercadoPagoClient(builder, properties, new ObjectMapper());
        MercadoPagoPreferencePayload payload = samplePayload();

        server.expect(requestTo("https://api.mercadopago.com/checkout/preferences"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "items": [
                            {
                              "title": "Producto",
                              "description": "Detalle",
                              "quantity": 1,
                              "currency_id": "PEN",
                              "unit_price": 10
                            }
                          ],
                          "back_urls": {
                            "success": "http://localhost:4200/pago/exito",
                            "failure": "http://localhost:4200/pago/fallo",
                            "pending": "http://localhost:4200/pago/pendiente"
                          },
                          "notification_url": "http://localhost:18080/api/v1/pagos/mercadopago/webhook",
                          "external_reference": "ORDEN-1",
                          "auto_return": "approved"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "pref_123",
                          "init_point": "https://mercadopago/init",
                          "sandbox_init_point": "https://mercadopago/sandbox"
                        }
                        """, MediaType.APPLICATION_JSON));

        MercadoPagoPreferenceResult result = client.createPreference(payload);

        assertThat(result.id()).isEqualTo("pref_123");
        assertThat(result.sandboxInitPoint()).isEqualTo("https://mercadopago/sandbox");
        server.verify();
    }

    @Test
    void obtenerPagoPorIdQueriesMercadoPagoPaymentEndpoint() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("dummy-token");
        properties.setBaseUrl("https://api.mercadopago.com");
        MercadoPagoClient client = new MercadoPagoClient(builder, properties, new ObjectMapper());

        server.expect(requestTo("https://api.mercadopago.com/v1/payments/166617913516"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token"))
                .andRespond(withSuccess("""
                        {
                          "id": 166617913516,
                          "status": "approved",
                          "external_reference": "ORDEN-83",
                          "transaction_amount": 100.00,
                          "currency_id": "PEN"
                        }
                        """, MediaType.APPLICATION_JSON));

        MercadoPagoPaymentResult result = client.obtenerPagoPorId("166617913516");

        assertThat(result.id()).isEqualTo("166617913516");
        assertThat(result.status()).isEqualTo("approved");
        assertThat(result.externalReference()).isEqualTo("ORDEN-83");
        assertThat(result.transactionAmount()).isEqualByComparingTo("100.00");
        assertThat(result.currencyId()).isEqualTo("PEN");
        server.verify();
    }

    private MercadoPagoPreferencePayload samplePayload() {
        return new MercadoPagoPreferencePayload(
                List.of(new MercadoPagoPreferencePayload.Item("Producto", "Detalle", 1, "PEN", BigDecimal.TEN)),
                new MercadoPagoPreferencePayload.BackUrls(
                        "http://localhost:4200/pago/exito",
                        "http://localhost:4200/pago/fallo",
                        "http://localhost:4200/pago/pendiente"
                ),
                "http://localhost:18080/api/v1/pagos/mercadopago/webhook",
                "ORDEN-1",
                "approved"
        );
    }
}
