package com.upeu.ordenes.dto;

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
public class OrdenResponse {

    private Long id;
    private Long idComprador;
    private Long idProducto;
    private Long idVendedor;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private String estado;
    private String metodoPago;
    private LocalDateTime createdAt;
}

