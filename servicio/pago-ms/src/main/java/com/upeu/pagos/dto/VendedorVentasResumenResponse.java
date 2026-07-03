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
public class VendedorVentasResumenResponse {

    private Long idVendedor;
    private long ventas;
    private BigDecimal montoTotal;
}
