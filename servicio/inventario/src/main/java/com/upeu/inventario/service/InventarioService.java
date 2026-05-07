package com.upeu.inventario.service;

import com.upeu.inventario.dto.InventarioRequest;
import com.upeu.inventario.dto.InventarioResponse;
import java.util.List;

public interface InventarioService {

    InventarioResponse create(InventarioRequest request);

    List<InventarioResponse> findAll();

    List<InventarioResponse> findByProducto(Long idProducto);

    InventarioResponse findById(Long id);

    InventarioResponse update(Long id, InventarioRequest request);

    void delete(Long id);
}

