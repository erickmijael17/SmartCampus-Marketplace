package com.upeu.pagos.repository;

import com.upeu.pagos.entity.Pago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByIdComprador(Long idComprador);

    Optional<Pago> findByExternalReference(String externalReference);

    Optional<Pago> findByIdOrden(Long idOrden);

    Optional<Pago> findByMpPaymentId(String mpPaymentId);

    long countByIdVendedorAndEstado(Long idVendedor, String estado);

    @Query("select coalesce(sum(p.monto), 0) from Pago p where p.idVendedor = :idVendedor and p.estado = :estado")
    BigDecimal sumMontoByIdVendedorAndEstado(@Param("idVendedor") Long idVendedor, @Param("estado") String estado);
}

