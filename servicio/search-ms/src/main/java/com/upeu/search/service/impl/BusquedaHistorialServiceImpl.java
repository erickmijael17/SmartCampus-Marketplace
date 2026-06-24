package com.upeu.search.service.impl;

import com.upeu.search.dto.BusquedaHistorialRequest;
import com.upeu.search.dto.BusquedaHistorialResponse;
import com.upeu.search.entity.BusquedaHistorial;
import com.upeu.search.repository.BusquedaHistorialRepository;
import com.upeu.search.service.BusquedaHistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusquedaHistorialServiceImpl implements BusquedaHistorialService {

    private final BusquedaHistorialRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<BusquedaHistorialResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BusquedaHistorialResponse findById(Long id) {
        BusquedaHistorial entity = repository.findById(id).orElseThrow(() -> new RuntimeException("BusquedaHistorial no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public BusquedaHistorialResponse create(BusquedaHistorialRequest request) {
        BusquedaHistorial entity = new BusquedaHistorial();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public BusquedaHistorialResponse update(Long id, BusquedaHistorialRequest request) {
        BusquedaHistorial entity = repository.findById(id).orElseThrow(() -> new RuntimeException("BusquedaHistorial no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(BusquedaHistorialRequest request, BusquedaHistorial entity) {
        entity.setIdUsuario(request.getIdUsuario());
        entity.setTermino(request.getTermino());
    }

    private BusquedaHistorialResponse mapToResponse(BusquedaHistorial entity) {
        BusquedaHistorialResponse response = new BusquedaHistorialResponse();
        response.setId(entity.getId());
        response.setIdUsuario(entity.getIdUsuario());
        response.setTermino(entity.getTermino());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
