package com.upeu.publicacion.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PublicacionRequest {
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private String estado;
    private Long idUsuario;
    private Long idCategoria;
}
