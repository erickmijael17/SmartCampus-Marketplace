package com.upeu.carrito.mapper;

import com.upeu.carrito.dto.CarritoRequest;
import com.upeu.carrito.dto.CarritoResponse;
import com.upeu.carrito.entity.Carrito;
import org.springframework.stereotype.Component;

@Component
public class CarritoMapper {

    public Carrito toEntity(CarritoRequest request) {
        return Carrito.builder()
                .idComprador(request.getIdComprador())
                .idProducto(request.getIdProducto())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .estado(request.getEstado())
                .build();
    }

    public CarritoResponse toResponse(Carrito entity) {
        return CarritoResponse.builder()
                .id(entity.getId())
                .idComprador(entity.getIdComprador())
                .idProducto(entity.getIdProducto())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntity(Carrito entity, CarritoRequest request) {
        entity.setIdComprador(request.getIdComprador());
        entity.setIdProducto(request.getIdProducto());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setEstado(request.getEstado());
    }
}
