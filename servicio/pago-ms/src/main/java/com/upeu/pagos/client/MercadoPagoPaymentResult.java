package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoPaymentResult(
        String id,
        String status,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("transaction_amount") BigDecimal transactionAmount,
        @JsonProperty("currency_id") String currencyId
) {
    public MercadoPagoPaymentResult(String id, String status, String externalReference) {
        this(id, status, externalReference, null, null);
    }
}
