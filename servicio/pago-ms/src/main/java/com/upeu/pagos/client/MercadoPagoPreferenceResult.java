package com.upeu.pagos.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoPreferenceResult(
        String id,
        @JsonProperty("init_point") String initPoint,
        @JsonProperty("sandbox_init_point") String sandboxInitPoint
) {
}
