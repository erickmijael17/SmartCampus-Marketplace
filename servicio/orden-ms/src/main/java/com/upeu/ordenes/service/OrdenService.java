package com.upeu.ordenes.service;

import com.upeu.ordenes.dto.ActualizarEstadoRequest;
import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import java.util.List;

public interface OrdenService {

    OrdenResponse create(OrdenRequest request);

    List<OrdenResponse> findAll();

    List<OrdenResponse> findByComprador(Long idComprador);

    OrdenResponse findById(Long id);

    OrdenResponse update(Long id, OrdenRequest request);

    OrdenResponse updateEstado(Long id, ActualizarEstadoRequest request);

    void delete(Long id);
}

