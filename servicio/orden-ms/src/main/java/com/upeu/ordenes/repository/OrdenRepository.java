package com.upeu.ordenes.repository;

import com.upeu.ordenes.entity.Orden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByIdComprador(Long idComprador);
}

