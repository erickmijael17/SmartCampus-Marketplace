package com.upeu.producto.mapper;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequest request) {
        if (request == null) {
            return null;
        }

        return Producto.builder()
            .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
            .precio(request.getPrecio())
            .moneda(request.getMoneda())
            .estado(request.getEstado())
                .idCategoria(request.getIdCategoria())
                .build();
    }

    public ProductoResponse toResponse(Producto entity) {
        if (entity == null) {
            return null;
        }

        return ProductoResponse.builder()
                .id(entity.getId())
            .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
            .precio(entity.getPrecio())
            .moneda(entity.getMoneda())
            .estado(entity.getEstado())
                .idCategoria(entity.getIdCategoria())
            .idVendedor(entity.getIdVendedor())
            .publicadoEn(entity.getPublicadoEn())
            .actualizadoEn(entity.getActualizadoEn())
                .build();
    }

    public void updateEntityFromRequest(Producto entity, ProductoRequest request) {
        entity.setTitulo(request.getTitulo());
        entity.setDescripcion(request.getDescripcion());
        entity.setPrecio(request.getPrecio());
        entity.setMoneda(request.getMoneda());
        entity.setEstado(request.getEstado());
        entity.setIdCategoria(request.getIdCategoria());
    }
}
