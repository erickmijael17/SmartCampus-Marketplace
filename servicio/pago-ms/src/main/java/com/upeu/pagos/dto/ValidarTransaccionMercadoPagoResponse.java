package com.upeu.pagos.dto;

import java.math.BigDecimal;
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
public class ValidarTransaccionMercadoPagoResponse {

    private Long pagoId;
    private Long idOrden;
    private String estado;
    private String mercadoPagoPaymentId;
    private String mpPaymentId;
    private String tituloProducto;
    private String preferenceId;
    private String externalReference;
    private BigDecimal monto;
    private String moneda;
    private String mensaje;
    private Boolean chatMessageCreated;
}
