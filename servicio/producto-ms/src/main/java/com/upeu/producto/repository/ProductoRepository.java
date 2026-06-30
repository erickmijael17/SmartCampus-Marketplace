package com.upeu.producto.repository;

import com.upeu.producto.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByIdVendedor(Long idVendedor);
    Optional<Producto> findByIdAndIdVendedor(Long id, Long idVendedor);
}
