package com.upeu.carrito.service;

import com.upeu.carrito.dto.CarritoRequest;
import com.upeu.carrito.dto.CarritoResponse;
import java.util.List;

public interface CarritoService {

    CarritoResponse create(CarritoRequest request);

    List<CarritoResponse> findAll();

    List<CarritoResponse> findByComprador(Long idComprador);

    CarritoResponse findById(Long id);

    CarritoResponse update(Long id, CarritoRequest request);

    void delete(Long id);
}
