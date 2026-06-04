package com.upeu.pagos.repository;

import com.upeu.pagos.entity.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByIdComprador(Long idComprador);
}

