package com.upeu.inventario.repository;

import com.upeu.inventario.entity.Inventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByIdProducto(Long idProducto);
}

