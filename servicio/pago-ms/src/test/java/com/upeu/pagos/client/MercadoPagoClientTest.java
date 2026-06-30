package com.upeu.pagos.client;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoClientTest {

    @Test
    void createPreferenceSendsBearerAccessToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("dummy-token");
        properties.setBaseUrl("https://api.mercadopago.com");
        MercadoPagoClient client = new MercadoPagoClient(builder, properties);
        MercadoPagoPreferencePayload payload = new MercadoPagoPreferencePayload(
                List.of(new MercadoPagoPreferencePayload.Item("Producto", "Detalle", 1, "PEN", BigDecimal.TEN)),
                new MercadoPagoPreferencePayload.BackUrls("http://localhost/success", "http://localhost/failure", "http://localhost/pending"),
                "http://localhost/webhook",
                "SCM-ORDEN-1-PAGO-2"
        );

        server.expect(requestTo("https://api.mercadopago.com/checkout/preferences"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token"))
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
}
