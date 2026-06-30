package com.upeu.publicacion.config;

import com.upeu.publicacion.entity.Publicacion;
import com.upeu.publicacion.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PublicacionRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Publicaciones ya existen. Saltando inicializacion.");
            return;
        }

        log.info("Sembrando publicaciones de demostracion...");

        repository.save(Publicacion.builder()
                .titulo("Laptop HP Pavilion")
                .descripcion("Laptop HP Pavilion 15 con 16GB RAM y 512GB SSD. Ideal para estudios.")
                .precio(new BigDecimal("2500.00"))
                .estado("ACTIVO")
                .idUsuario(1L)
                .idCategoria(2L)
                .build());

        repository.save(Publicacion.builder()
                .titulo("Calculadora Cientifica Casio")
                .descripcion("Calculadora Casio FX-991LAX en perfecto estado.")
                .precio(new BigDecimal("80.00"))
                .estado("ACTIVO")
                .idUsuario(1L)
                .idCategoria(1L)
                .build());

        repository.save(Publicacion.builder()
                .titulo("Libro de Algoritmos")
                .descripcion("Introduccion a algoritmos - CLRS. Tapa blanda.")
                .precio(new BigDecimal("120.00"))
                .estado("ACTIVO")
                .idUsuario(1L)
                .idCategoria(1L)
                .build());

        log.info("{} publicaciones de demostracion creadas.", repository.count());
    }
}
