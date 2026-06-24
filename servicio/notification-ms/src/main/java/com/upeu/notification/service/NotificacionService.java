package com.upeu.notification.service;

import com.upeu.notification.dto.NotificacionRequest;
import com.upeu.notification.dto.NotificacionResponse;
import java.util.List;

public interface NotificacionService {
    List<NotificacionResponse> findAll();
    NotificacionResponse findById(Long id);
    NotificacionResponse create(NotificacionRequest request);
    NotificacionResponse update(Long id, NotificacionRequest request);
    void delete(Long id);
}
