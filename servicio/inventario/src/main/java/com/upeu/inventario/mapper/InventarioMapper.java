package com.upeu.inventario.mapper;

import com.upeu.inventario.dto.InventarioRequest;
import com.upeu.inventario.dto.InventarioResponse;
import com.upeu.inventario.entity.Inventario;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    public Inventario toEntity(InventarioRequest request) {
        return Inventario.builder()
                .idProducto(request.getIdProducto())
                .stockDisponible(request.getStockDisponible())
                .stockReservado(request.getStockReservado())
                .estado(request.getEstado())
                .build();
    }

    public InventarioResponse toResponse(Inventario entity) {
        return InventarioResponse.builder()
                .id(entity.getId())
                .idProducto(entity.getIdProducto())
                .stockDisponible(entity.getStockDisponible())
                .stockReservado(entity.getStockReservado())
                .estado(entity.getEstado())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(Inventario entity, InventarioRequest request) {
        entity.setIdProducto(request.getIdProducto());
        entity.setStockDisponible(request.getStockDisponible());
        entity.setStockReservado(request.getStockReservado());
        entity.setEstado(request.getEstado());
    }
}

