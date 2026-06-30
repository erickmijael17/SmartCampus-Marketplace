package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoPaymentResult(
        String id,
        String status,
        @JsonProperty("external_reference") String externalReference
) {
}
