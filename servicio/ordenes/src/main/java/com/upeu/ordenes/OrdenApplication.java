package com.upeu.ordenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrdenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenApplication.class, args);
    }
}

