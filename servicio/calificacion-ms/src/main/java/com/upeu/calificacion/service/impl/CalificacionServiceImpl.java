package com.upeu.calificacion.service.impl;

import com.upeu.calificacion.dto.CalificacionRequest;
import com.upeu.calificacion.dto.CalificacionResponse;
import com.upeu.calificacion.entity.Calificacion;
import com.upeu.calificacion.repository.CalificacionRepository;
import com.upeu.calificacion.service.CalificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<CalificacionResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CalificacionResponse findById(Long id) {
        Calificacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Calificacion no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public CalificacionResponse create(CalificacionRequest request) {
        Calificacion entity = new Calificacion();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public CalificacionResponse update(Long id, CalificacionRequest request) {
        Calificacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Calificacion no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(CalificacionRequest request, Calificacion entity) {
        entity.setPuntuacion(request.getPuntuacion());
        entity.setComentario(request.getComentario());
        entity.setIdUsuario(request.getIdUsuario());
        entity.setIdPublicacion(request.getIdPublicacion());
    }

    private CalificacionResponse mapToResponse(Calificacion entity) {
        CalificacionResponse response = new CalificacionResponse();
        response.setId(entity.getId());
        response.setPuntuacion(entity.getPuntuacion());
        response.setComentario(entity.getComentario());
        response.setIdUsuario(entity.getIdUsuario());
        response.setIdPublicacion(entity.getIdPublicacion());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
