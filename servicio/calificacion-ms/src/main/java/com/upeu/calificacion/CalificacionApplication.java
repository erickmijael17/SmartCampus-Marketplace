package com.upeu.calificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CalificacionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalificacionApplication.class, args);
    }
}
