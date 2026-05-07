package com.upeu.pagos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pagosOpenApi() {
        return new OpenAPI().info(new Info()
                .title("pagos API")
                .description("API REST del microservicio de pagos")
                .version("1.0.0")
                .contact(new Contact().name("Equipo pagos").email("pagos@upeu.edu.pe"))
                .license(new License().name("Internal Use Only").url("https://upeu.edu.pe")));
    }
}

