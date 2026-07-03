package com.upeu.categoria.service.impl;

import com.upeu.categoria.dto.CategoriaRequest;
import com.upeu.categoria.dto.CategoriaResponse;
import com.upeu.categoria.entity.Categoria;
import com.upeu.categoria.repository.CategoriaRepository;
import com.upeu.categoria.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse findById(Long id) {
        Categoria entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Categoria no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        Categoria entity = new Categoria();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public CategoriaResponse update(Long id, CategoriaRequest request) {
        Categoria entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Categoria no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(CategoriaRequest request, Categoria entity) {
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
    }

    private CategoriaResponse mapToResponse(Categoria entity) {
        CategoriaResponse response = new CategoriaResponse();
        response.setId(entity.getId());
        response.setNombre(entity.getNombre());
        response.setDescripcion(entity.getDescripcion());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
