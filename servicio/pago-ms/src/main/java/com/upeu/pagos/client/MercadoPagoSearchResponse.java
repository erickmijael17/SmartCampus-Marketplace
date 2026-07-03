package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoSearchResponse(
        List<MercadoPagoPaymentResult> results
) {}
