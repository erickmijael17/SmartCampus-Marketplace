package com.upeu.notification.service.impl;

import com.upeu.notification.dto.NotificacionRequest;
import com.upeu.notification.dto.NotificacionResponse;
import com.upeu.notification.entity.Notificacion;
import com.upeu.notification.repository.NotificacionRepository;
import com.upeu.notification.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionResponse findById(Long id) {
        Notificacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Notificacion no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public NotificacionResponse create(NotificacionRequest request) {
        Notificacion entity = new Notificacion();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public NotificacionResponse update(Long id, NotificacionRequest request) {
        Notificacion entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Notificacion no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(NotificacionRequest request, Notificacion entity) {
        entity.setIdUsuario(request.getIdUsuario());
        entity.setTitulo(request.getTitulo());
        entity.setMensaje(request.getMensaje());
        entity.setLeido(request.getLeido());
    }

    private NotificacionResponse mapToResponse(Notificacion entity) {
        NotificacionResponse response = new NotificacionResponse();
        response.setId(entity.getId());
        response.setIdUsuario(entity.getIdUsuario());
        response.setTitulo(entity.getTitulo());
        response.setMensaje(entity.getMensaje());
        response.setLeido(entity.getLeido());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
