package com.upeu.producto.service;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {

    ProductoResponse create(ProductoRequest request, String token);

    List<ProductoResponse> findAll();

    ProductoResponse findById(Long id);

    ProductoResponse update(Long id, ProductoRequest request, String token);

    void delete(Long id, String token);

    ProductoResponse findDetalleById(Long id);
}
