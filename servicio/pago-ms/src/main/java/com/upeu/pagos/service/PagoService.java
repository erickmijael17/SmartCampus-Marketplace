package com.upeu.pagos.service;

import com.upeu.pagos.dto.PagoRequest;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.dto.VendedorVentasResumenResponse;
import java.util.List;

public interface PagoService {

    PagoResponse create(PagoRequest request);

    List<PagoResponse> findAll();

    List<PagoResponse> findByComprador(Long idComprador);

    PagoResponse findById(Long id);

    VendedorVentasResumenResponse getResumenVentasVendedor(Long idVendedor);

    PagoResponse update(Long id, PagoRequest request);

    void delete(Long id);
}

