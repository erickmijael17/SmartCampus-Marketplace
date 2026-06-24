package com.upeu.categoria.service;

import com.upeu.categoria.dto.CategoriaRequest;
import com.upeu.categoria.dto.CategoriaResponse;
import java.util.List;

public interface CategoriaService {
    List<CategoriaResponse> findAll();
    CategoriaResponse findById(Long id);
    CategoriaResponse create(CategoriaRequest request);
    CategoriaResponse update(Long id, CategoriaRequest request);
    void delete(Long id);
}
