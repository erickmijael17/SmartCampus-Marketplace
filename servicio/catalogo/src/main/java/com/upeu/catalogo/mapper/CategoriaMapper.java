package com.upeu.catalogo.mapper;

import com.upeu.catalogo.dto.CategoriaRequest;
import com.upeu.catalogo.dto.CategoriaResponse;
import com.upeu.catalogo.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toEntity(CategoriaRequest request) {
        if (request == null) {
            return null;
        }

        return Categoria.builder()
            .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
            .activo(request.getActivo() == null ? Boolean.TRUE : request.getActivo())
                .build();
    }

    public CategoriaResponse toResponse(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return CategoriaResponse.builder()
                .id(entity.getId())
            .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
            .activo(entity.getActivo())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(Categoria entity, CategoriaRequest request) {
        entity.setCodigo(request.getCodigo());
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setActivo(request.getActivo() == null ? entity.getActivo() : request.getActivo());
    }
}
