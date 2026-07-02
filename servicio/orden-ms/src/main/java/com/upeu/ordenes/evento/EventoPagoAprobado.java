package com.upeu.ordenes.evento;

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
public class EventoPagoAprobado {

    private String eventId;

    @JsonProperty("eventType")
    @JsonAlias("tipoEvento")
    private String tipoEvento;

    private Long pagoId;
    private Long ordenId;

    @JsonAlias("compradorId")
    private Long idComprador;

    @JsonAlias("vendedorId")
    private Long idVendedor;

    @JsonAlias("publicacionId")
    private Long productoId;

    private Long publicacionId;
    private String tituloProducto;
    private Double monto;
    private String moneda;
    private String mpPaymentId;

    @JsonAlias("estado")
    private String estadoPago;

    private String origen;
    private Long timestamp;

    public String getEventType() {
        return tipoEvento;
    }
}
