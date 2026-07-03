package com.upeu.publicacion.service.impl;

import com.upeu.publicacion.dto.PublicacionRequest;
import com.upeu.publicacion.dto.PublicacionResponse;
import com.upeu.publicacion.entity.Publicacion;
import com.upeu.publicacion.repository.PublicacionRepository;
import com.upeu.publicacion.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponse findById(Long id) {
        Publicacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Publicacion no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public PublicacionResponse create(PublicacionRequest request) {
        Publicacion entity = new Publicacion();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public PublicacionResponse update(Long id, PublicacionRequest request) {
        Publicacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Publicacion no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(PublicacionRequest request, Publicacion entity) {
        entity.setTitulo(request.getTitulo());
        entity.setDescripcion(request.getDescripcion());
        entity.setPrecio(request.getPrecio());
        entity.setEstado(request.getEstado());
        entity.setIdUsuario(request.getIdUsuario());
        entity.setIdCategoria(request.getIdCategoria());
    }

    private PublicacionResponse mapToResponse(Publicacion entity) {
        PublicacionResponse response = new PublicacionResponse();
        response.setId(entity.getId());
        response.setTitulo(entity.getTitulo());
        response.setDescripcion(entity.getDescripcion());
        response.setPrecio(entity.getPrecio());
        response.setEstado(entity.getEstado());
        response.setIdUsuario(entity.getIdUsuario());
        response.setIdCategoria(entity.getIdCategoria());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
