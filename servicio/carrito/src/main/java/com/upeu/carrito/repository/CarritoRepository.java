package com.upeu.carrito.repository;

import com.upeu.carrito.entity.Carrito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByIdComprador(Long idComprador);
}
