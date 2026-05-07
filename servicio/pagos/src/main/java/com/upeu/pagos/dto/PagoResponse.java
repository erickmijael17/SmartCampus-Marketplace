package com.upeu.pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class PagoResponse {

    private Long id;
    private Long idOrden;
    private Long idComprador;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String referenciaTransaccion;
    private LocalDateTime createdAt;
}

