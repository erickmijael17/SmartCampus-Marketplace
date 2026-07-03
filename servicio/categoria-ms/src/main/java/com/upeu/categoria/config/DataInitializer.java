package com.upeu.categoria.config;

import com.upeu.categoria.entity.Categoria;
import com.upeu.categoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Categorias ya existen en la base de datos. Saltando inicializacion.");
            return;
        }

        log.info("Sembrando categorias iniciales...");

        repository.save(Categoria.builder().nombre("Libros y Apuntes").descripcion("Material academico").build());
        repository.save(Categoria.builder().nombre("Electronica").descripcion("Dispositivos y accesorios").build());
        repository.save(Categoria.builder().nombre("Ropa").descripcion("Vestimenta universitaria").build());
        repository.save(Categoria.builder().nombre("Servicios").descripcion("Tutorias y servicios estudiantiles").build());
        repository.save(Categoria.builder().nombre("Alimentos").descripcion("Comida y bebidas").build());
        repository.save(Categoria.builder().nombre("Otros").descripcion("Articulos varios").build());
        repository.save(Categoria.builder().nombre("Accesorios").descripcion("Mochilas, audifonos, cargadores y articulos complementarios").build());
        repository.save(Categoria.builder().nombre("Deporte").descripcion("Implementos deportivos y articulos para actividades universitarias").build());
        repository.save(Categoria.builder().nombre("Tutorias").descripcion("Apoyo academico entre estudiantes y docentes").build());
        repository.save(Categoria.builder().nombre("Movilidad").descripcion("Bicicletas, scooters y transporte compartido").build());
        repository.save(Categoria.builder().nombre("Libros").descripcion("Libros de texto, novelas y material de lectura").build());
        repository.save(Categoria.builder().nombre("Ropas").descripcion("Prendas de vestir, uniformes y accesorios de moda").build());

        log.info("{} categorias sembradas exitosamente.", repository.count());
    }
}
