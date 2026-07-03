package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MercadoPagoPreferencePayload(
        List<Item> items,
        @JsonProperty("back_urls") BackUrls backUrls,
        @JsonProperty("notification_url") String notificationUrl,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("auto_return") String autoReturn
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
