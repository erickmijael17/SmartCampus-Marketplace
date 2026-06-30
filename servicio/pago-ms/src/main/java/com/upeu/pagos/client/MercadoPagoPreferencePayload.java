package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record MercadoPagoPreferencePayload(
        List<Item> items,
        @JsonProperty("back_urls") BackUrls backUrls,
        @JsonProperty("notification_url") String notificationUrl,
        @JsonProperty("external_reference") String externalReference
) {
    public record Item(
            String title,
            String description,
            Integer quantity,
            @JsonProperty("currency_id") String currencyId,
            @JsonProperty("unit_price") BigDecimal unitPrice
    ) {
    }

    public record BackUrls(
            String success,
            String failure,
            String pending
    ) {
    }
}
