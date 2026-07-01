package com.upeu.chat.repository;

import com.upeu.chat.entity.Conversacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    @Query("""
            select c from Conversacion c
            where (c.idUsuario1 = :usuario1 and c.idUsuario2 = :usuario2)
               or (c.idUsuario1 = :usuario2 and c.idUsuario2 = :usuario1)
            """)
    Optional<Conversacion> findBetweenUsers(@Param("usuario1") Long usuario1, @Param("usuario2") Long usuario2);
}
