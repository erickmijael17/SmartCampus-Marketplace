package com.upeu.search.service;

import com.upeu.search.dto.BusquedaHistorialRequest;
import com.upeu.search.dto.BusquedaHistorialResponse;
import java.util.List;

public interface BusquedaHistorialService {
    List<BusquedaHistorialResponse> findAll();
    BusquedaHistorialResponse findById(Long id);
    BusquedaHistorialResponse create(BusquedaHistorialRequest request);
    BusquedaHistorialResponse update(Long id, BusquedaHistorialRequest request);
    void delete(Long id);
}
