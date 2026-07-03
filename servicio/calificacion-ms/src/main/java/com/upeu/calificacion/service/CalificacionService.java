package com.upeu.calificacion.service;

import com.upeu.calificacion.dto.CalificacionRequest;
import com.upeu.calificacion.dto.CalificacionResponse;
import java.util.List;

public interface CalificacionService {
    List<CalificacionResponse> findAll();
    CalificacionResponse findById(Long id);
    CalificacionResponse create(CalificacionRequest request);
    CalificacionResponse update(Long id, CalificacionRequest request);
    void delete(Long id);
}
