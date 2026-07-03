package com.upeu.favoritos.service.impl;

import com.upeu.favoritos.dto.FavoritoRequest;
import com.upeu.favoritos.dto.FavoritoResponse;
import com.upeu.favoritos.entity.Favorito;
import com.upeu.favoritos.repository.FavoritoRepository;
import com.upeu.favoritos.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<FavoritoResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FavoritoResponse findById(Long id) {
        Favorito entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public FavoritoResponse create(FavoritoRequest request) {
        Favorito entity = new Favorito();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public FavoritoResponse update(Long id, FavoritoRequest request) {
        Favorito entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(FavoritoRequest request, Favorito entity) {
        entity.setIdUsuario(request.getIdUsuario());
        entity.setIdPublicacion(request.getIdPublicacion());
    }

    private FavoritoResponse mapToResponse(Favorito entity) {
        FavoritoResponse response = new FavoritoResponse();
        response.setId(entity.getId());
        response.setIdUsuario(entity.getIdUsuario());
        response.setIdPublicacion(entity.getIdPublicacion());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
