package com.upeu.categoria.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
