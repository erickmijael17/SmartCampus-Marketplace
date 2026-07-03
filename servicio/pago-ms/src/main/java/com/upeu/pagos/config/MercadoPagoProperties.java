package com.upeu.pagos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mercadopago")
public class MercadoPagoProperties {

    private String accessToken;
    private String publicKey;
    private String baseUrl = "https://api.mercadopago.com";
    private String notificationUrl;
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
    private boolean autoReturnEnabled = false;
}
