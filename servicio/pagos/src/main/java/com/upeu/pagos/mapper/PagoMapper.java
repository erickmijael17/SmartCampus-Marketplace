package com.upeu.pagos.mapper;

import com.upeu.pagos.dto.PagoRequest;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public Pago toEntity(PagoRequest request) {
        return Pago.builder()
                .idOrden(request.getIdOrden())
                .idComprador(request.getIdComprador())
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago())
                .estado(request.getEstado())
                .referenciaTransaccion(request.getReferenciaTransaccion())
                .build();
    }

    public PagoResponse toResponse(Pago entity) {
        return PagoResponse.builder()
                .id(entity.getId())
                .idOrden(entity.getIdOrden())
                .idComprador(entity.getIdComprador())
                .monto(entity.getMonto())
                .metodoPago(entity.getMetodoPago())
                .estado(entity.getEstado())
                .referenciaTransaccion(entity.getReferenciaTransaccion())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntity(Pago entity, PagoRequest request) {
        entity.setIdOrden(request.getIdOrden());
        entity.setIdComprador(request.getIdComprador());
        entity.setMonto(request.getMonto());
        entity.setMetodoPago(request.getMetodoPago());
        entity.setEstado(request.getEstado());
        entity.setReferenciaTransaccion(request.getReferenciaTransaccion());
    }
}

