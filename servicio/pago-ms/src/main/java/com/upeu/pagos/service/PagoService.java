package com.upeu.pagos.service;

import com.upeu.pagos.dto.PagoRequest;
import com.upeu.pagos.dto.PagoResponse;
import java.util.List;

public interface PagoService {

    PagoResponse create(PagoRequest request);

    List<PagoResponse> findAll();

    List<PagoResponse> findByComprador(Long idComprador);

    PagoResponse findById(Long id);

    PagoResponse update(Long id, PagoRequest request);

    void delete(Long id);
}

