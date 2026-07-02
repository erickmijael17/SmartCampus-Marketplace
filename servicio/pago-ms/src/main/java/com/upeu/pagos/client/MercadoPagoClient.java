package com.upeu.pagos.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upeu.pagos.config.MercadoPagoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class MercadoPagoClient {

    private final MercadoPagoProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoClient(RestClient.Builder builder, MercadoPagoProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    }

    public MercadoPagoPreferenceResult createPreference(MercadoPagoPreferencePayload payload) {
        String requestBody = serializePayload(payload);
        log.info("MP preference payload={}", requestBody);
        try {
            return restClient
                    .post()
                    .uri("/checkout/preferences")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(MercadoPagoPreferenceResult.class);
        } catch (RestClientResponseException e) {
            log.error(
                    "Mercado Pago rechazo preferencia status={}, body={}, external_reference={}, monto={}, titulo={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    payload.externalReference(),
                    firstItemUnitPrice(payload),
                    firstItemTitle(payload)
            );
            throw e;
        }
    }

    public MercadoPagoPaymentResult getPayment(String paymentId) {
        return obtenerPagoPorId(paymentId);
    }

    public MercadoPagoPaymentResult obtenerPagoPorId(String paymentId) {
        return restClient
                .get()
                .uri("/v1/payments/{id}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                .retrieve()
                .body(MercadoPagoPaymentResult.class);
    }

    private String serializePayload(MercadoPagoPreferencePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la preferencia de Mercado Pago", e);
        }
    }

    private Object firstItemUnitPrice(MercadoPagoPreferencePayload payload) {
        if (payload.items() == null || payload.items().isEmpty()) {
            return null;
        }
        return payload.items().get(0).unitPrice();
    }

    private String firstItemTitle(MercadoPagoPreferencePayload payload) {
        if (payload.items() == null || payload.items().isEmpty()) {
            return null;
        }
        return payload.items().get(0).title();
    }
}
