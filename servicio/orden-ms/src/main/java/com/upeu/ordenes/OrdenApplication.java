package com.upeu.ordenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.upeu.ordenes.config.JwtProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(JwtProperties.class)
public class OrdenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenApplication.class, args);
    }
}

