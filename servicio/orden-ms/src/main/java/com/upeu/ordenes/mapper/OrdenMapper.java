package com.upeu.ordenes.mapper;

import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.entity.Orden;
import org.springframework.stereotype.Component;

@Component
public class OrdenMapper {

    public Orden toEntity(OrdenRequest request) {
        return Orden.builder()
                .idComprador(request.getIdComprador())
                .idProducto(request.getIdProducto())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .estado(request.getEstado())
                .build();
    }

    public OrdenResponse toResponse(Orden entity) {
        return OrdenResponse.builder()
                .id(entity.getId())
                .idComprador(entity.getIdComprador())
                .idProducto(entity.getIdProducto())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntity(Orden entity, OrdenRequest request) {
        entity.setIdComprador(request.getIdComprador());
        entity.setIdProducto(request.getIdProducto());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setEstado(request.getEstado());
    }
}

