package com.upeu.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoConfirmacionResponse {
    private Long ordenId;
    private String estadoPago;
    private String estadoOrden;
    private Long chatId;
    private boolean mensajeComprobanteEnviado;
}
