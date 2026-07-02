package com.upeu.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.upeu.pagos.config.AppProperties;
import com.upeu.pagos.config.JwtProperties;
import com.upeu.pagos.config.MercadoPagoProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class, MercadoPagoProperties.class})
public class PagoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoApplication.class, args);
    }
}

