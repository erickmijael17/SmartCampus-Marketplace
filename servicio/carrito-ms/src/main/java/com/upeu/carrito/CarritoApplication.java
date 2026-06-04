package com.upeu.carrito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.upeu.carrito.config.JwtProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(JwtProperties.class)
public class CarritoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarritoApplication.class, args);
    }
}
