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
    private Long pagoId;
    private Long ordenId;
    private String estado;
    private String estadoPago;
    private String estadoOrden;
    private Long chatId;
    private Long conversacionId;
    private String mensaje;
    private boolean mensajeComprobanteEnviado;
}
