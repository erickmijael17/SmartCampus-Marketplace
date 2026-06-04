package com.upeu.ordenes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ordenesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ordenes API")
                .description("API REST del microservicio de ordenes")
                .version("1.0.0")
                .contact(new Contact().name("Equipo ordenes").email("ordenes@upeu.edu.pe"))
                .license(new License().name("Internal Use Only").url("https://upeu.edu.pe")));
    }
}

