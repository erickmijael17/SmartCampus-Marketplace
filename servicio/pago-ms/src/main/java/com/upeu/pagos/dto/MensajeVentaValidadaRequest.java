package com.upeu.pagos.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeVentaValidadaRequest {

    private Long idComprador;
    private Long idVendedor;
    private Long publicacionId;
    private Long idOrden;
    private Long pagoId;
    private String tituloProducto;
    private BigDecimal monto;
    private String moneda;
    private String mpPaymentId;
}
