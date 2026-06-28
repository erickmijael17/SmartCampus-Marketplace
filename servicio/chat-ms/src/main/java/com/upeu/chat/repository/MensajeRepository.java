package com.upeu.chat.repository;

import com.upeu.chat.entity.Mensaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByIdConversacionOrderByCreadoEnAsc(Long idConversacion);
}
