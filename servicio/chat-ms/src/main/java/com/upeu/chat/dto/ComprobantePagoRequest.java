package com.upeu.chat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePagoRequest {
    private Long ordenId;
    private Long idComprador;
    private Long idVendedor;
    private Long publicacionId;
    private String tituloProducto;
    private BigDecimal monto;
    private String moneda;
    private String estadoPago;
    private String metodoPago;
    private String paymentId;
    private LocalDateTime fechaPago;
}
