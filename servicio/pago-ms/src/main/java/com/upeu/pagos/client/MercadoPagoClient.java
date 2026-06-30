package com.upeu.pagos.client;

import com.upeu.pagos.config.MercadoPagoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MercadoPagoClient {

    private final MercadoPagoProperties properties;
    private final RestClient restClient;

    public MercadoPagoClient(RestClient.Builder builder, MercadoPagoProperties properties) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    }

    public MercadoPagoPreferenceResult createPreference(MercadoPagoPreferencePayload payload) {
        return restClient
                .post()
                .uri("/checkout/preferences")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                .body(payload)
                .retrieve()
                .body(MercadoPagoPreferenceResult.class);
    }

    public MercadoPagoPaymentResult getPayment(String paymentId) {
        return restClient
                .get()
                .uri("/v1/payments/{id}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                .retrieve()
                .body(MercadoPagoPaymentResult.class);
    }
}
