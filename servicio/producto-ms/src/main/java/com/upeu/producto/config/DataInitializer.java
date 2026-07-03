package com.upeu.producto.config;

import com.upeu.producto.entity.Producto;
import com.upeu.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Productos ya existen. Saltando inicializacion.");
            return;
        }

        log.info("Sembrando productos de demostracion...");

        repository.save(Producto.builder()
                .titulo("Laptop HP Pavilion")
                .descripcion("Laptop HP Pavilion 15 con 16GB RAM y 512GB SSD.")
                .precio(new BigDecimal("2500.00"))
                .moneda("PEN")
                .estado("ACTIVO")
                .idCategoria(2L)
                .idVendedor(1L)
                .build());

        repository.save(Producto.builder()
                .titulo("Calculadora Cientifica Casio")
                .descripcion("Calculadora Casio FX-991LAX en perfecto estado.")
                .precio(new BigDecimal("80.00"))
                .moneda("PEN")
                .estado("ACTIVO")
                .idCategoria(1L)
                .idVendedor(1L)
                .build());

        repository.save(Producto.builder()
                .titulo("Libro de Algoritmos")
                .descripcion("Introduccion a algoritmos - CLRS.")
                .precio(new BigDecimal("120.00"))
                .moneda("PEN")
                .estado("ACTIVO")
                .idCategoria(1L)
                .idVendedor(1L)
                .build());

        log.info("{} productos de demostracion creados.", repository.count());
    }
}
