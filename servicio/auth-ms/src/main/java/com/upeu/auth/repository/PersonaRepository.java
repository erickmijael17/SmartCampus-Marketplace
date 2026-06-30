package com.upeu.auth.repository;

import com.upeu.auth.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByUserId(String userId);
    Optional<Persona> findByEmail(String email);
    Optional<Persona> findByCodigoUniversitario(String codigoUniversitario);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
}
