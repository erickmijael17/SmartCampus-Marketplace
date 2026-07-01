package com.upeu.pagos.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoConfig {

    private final MercadoPagoProperties properties;

    @PostConstruct
    void validateCredentials() {
        if (properties.getAccessToken() == null || properties.getAccessToken().isBlank()) {
            throw new IllegalStateException("MP_ACCESS_TOKEN no esta configurado para Mercado Pago Checkout Pro.");
        }
        log.info("Mercado Pago Access Token cargado: true");
        log.info("Mercado Pago token length: {}", properties.getAccessToken().length());
        log.info("Mercado Pago token prefix: {}", safePrefix(properties.getAccessToken()));
        com.mercadopago.MercadoPagoConfig.setAccessToken(properties.getAccessToken());
    }

    private String safePrefix(String token) {
        int prefixLength = Math.min(8, token.length());
        return token.substring(0, prefixLength) + "...";
    }
}
