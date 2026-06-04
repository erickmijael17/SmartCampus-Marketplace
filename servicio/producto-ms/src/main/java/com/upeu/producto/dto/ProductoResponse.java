package com.upeu.producto.dto;

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
public class ProductoResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private String moneda;
    private String estado;
    private Long idCategoria;
    private Long idVendedor;
    private LocalDateTime publicadoEn;
    private LocalDateTime actualizadoEn;
    private CategoriaDto categoria;
}
