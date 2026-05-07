package com.upeu.carrito.service.impl;

import com.upeu.carrito.dto.CarritoRequest;
import com.upeu.carrito.dto.CarritoResponse;
import com.upeu.carrito.entity.Carrito;
import com.upeu.carrito.exception.ResourceNotFoundException;
import com.upeu.carrito.mapper.CarritoMapper;
import com.upeu.carrito.repository.CarritoRepository;
import com.upeu.carrito.service.CarritoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoMapper carritoMapper;

    @Override
    @Transactional
    public CarritoResponse create(CarritoRequest request) {
        Carrito carrito = carritoMapper.toEntity(request);
        return carritoMapper.toResponse(carritoRepository.save(carrito));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarritoResponse> findAll() {
        return carritoRepository.findAll().stream().map(carritoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarritoResponse> findByComprador(Long idComprador) {
        return carritoRepository.findByIdComprador(idComprador).stream().map(carritoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoResponse findById(Long id) {
        return carritoMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public CarritoResponse update(Long id, CarritoRequest request) {
        Carrito entity = getEntity(id);
        carritoMapper.updateEntity(entity, request);
        return carritoMapper.toResponse(carritoRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        carritoRepository.deleteById(id);
    }

    private Carrito getEntity(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito con id " + id + " no encontrado"));
    }
}
