package com.upeu.ordenes.service.impl;

import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.entity.Orden;
import com.upeu.ordenes.exception.ResourceNotFoundException;
import com.upeu.ordenes.mapper.OrdenMapper;
import com.upeu.ordenes.repository.OrdenRepository;
import com.upeu.ordenes.service.OrdenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenesRepository;
    private final OrdenMapper ordenesMapper;

    @Override
    @Transactional
    public OrdenResponse create(OrdenRequest request) {
        Orden ordenes = ordenesMapper.toEntity(request);
        return ordenesMapper.toResponse(ordenesRepository.save(ordenes));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> findAll() {
        return ordenesRepository.findAll().stream().map(ordenesMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> findByComprador(Long idComprador) {
        return ordenesRepository.findByIdComprador(idComprador).stream().map(ordenesMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponse findById(Long id) {
        return ordenesMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public OrdenResponse update(Long id, OrdenRequest request) {
        Orden entity = getEntity(id);
        ordenesMapper.updateEntity(entity, request);
        return ordenesMapper.toResponse(ordenesRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        ordenesRepository.deleteById(id);
    }

    private Orden getEntity(Long id) {
        return ordenesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden con id " + id + " no encontrado"));
    }
}

