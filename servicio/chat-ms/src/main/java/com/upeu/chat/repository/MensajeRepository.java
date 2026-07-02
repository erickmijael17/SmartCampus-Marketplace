package com.upeu.chat.repository;

import com.upeu.chat.entity.Mensaje;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByIdConversacionOrderByCreadoEnAsc(Long idConversacion);

    boolean existsByIdConversacionAndIdOrdenAndTipoMensaje(Long idConversacion, Long idOrden, String tipoMensaje);

    boolean existsByIdConversacionAndIdOrdenAndPagoIdAndTipoMensaje(
            Long idConversacion,
            Long idOrden,
            Long pagoId,
            String tipoMensaje
    );

    Optional<Mensaje> findFirstByIdConversacionAndIdOrdenAndPagoIdAndMpPaymentIdAndTipoMensaje(
            Long idConversacion,
            Long idOrden,
            Long pagoId,
            String mpPaymentId,
            String tipoMensaje
    );
}
