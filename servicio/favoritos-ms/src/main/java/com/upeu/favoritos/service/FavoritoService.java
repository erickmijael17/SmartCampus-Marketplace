package com.upeu.favoritos.service;

import com.upeu.favoritos.dto.FavoritoRequest;
import com.upeu.favoritos.dto.FavoritoResponse;
import java.util.List;

public interface FavoritoService {
    List<FavoritoResponse> findAll();
    FavoritoResponse findById(Long id);
    FavoritoResponse create(FavoritoRequest request);
    FavoritoResponse update(Long id, FavoritoRequest request);
    void delete(Long id);
}
