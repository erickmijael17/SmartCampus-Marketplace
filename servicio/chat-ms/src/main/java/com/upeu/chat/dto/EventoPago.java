package com.upeu.chat.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoPago {

    @JsonProperty("eventType")
    @JsonAlias("tipoEvento")
    private String tipoEvento;
    private Long pagoId;
    private Long ordenId;
    private Long idComprador;
    private String nombreComprador;
    private Long idVendedor;
    private Long publicacionId;
    private String tituloProducto;
    private Double monto;
    private String moneda;
    private String mpPaymentId;
    private String estado;
    private String origen;
    private Long timestamp;
}
