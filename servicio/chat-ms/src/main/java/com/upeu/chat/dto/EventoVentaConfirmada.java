package com.upeu.chat.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoVentaConfirmada {

    private String eventId;

    @JsonProperty("eventType")
    @JsonAlias("tipoEvento")
    private String eventType;

    private Long ordenId;
    private Long pagoId;
    private Long productoId;
    private Long publicacionId;
    private String tituloProducto;
    private Long compradorId;
    private Long vendedorId;
    private BigDecimal precio;
    private String moneda;
    private String estadoPago;
    private String timestamp;
}
