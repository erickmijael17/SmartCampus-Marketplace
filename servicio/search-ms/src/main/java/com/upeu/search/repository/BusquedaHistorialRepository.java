package com.upeu.search.repository;

import com.upeu.search.entity.BusquedaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusquedaHistorialRepository extends JpaRepository<BusquedaHistorial, Long> {
}
