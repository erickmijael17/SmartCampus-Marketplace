package com.upeu.publicacion.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class PublicacionResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private String estado;
    private Long idUsuario;
    private Long idCategoria;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
