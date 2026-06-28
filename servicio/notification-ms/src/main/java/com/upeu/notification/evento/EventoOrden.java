package com.upeu.notification.evento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoOrden {

    private String tipoEvento;
    private Long ordenId;
    private Long idComprador;
    private Double total;
    private String estado;
    private String origen;
    private Long timestamp;
}
