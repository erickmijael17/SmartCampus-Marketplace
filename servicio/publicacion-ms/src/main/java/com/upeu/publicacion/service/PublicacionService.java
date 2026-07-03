package com.upeu.publicacion.service;

import com.upeu.publicacion.dto.PublicacionRequest;
import com.upeu.publicacion.dto.PublicacionResponse;
import java.util.List;

public interface PublicacionService {
    List<PublicacionResponse> findAll();
    PublicacionResponse findById(Long id);
    PublicacionResponse create(PublicacionRequest request);
    PublicacionResponse update(Long id, PublicacionRequest request);
    void delete(Long id);
}
