package com.upeu.calificacion.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalificacionResponse {
    private Long id;
    private Integer puntuacion;
    private String comentario;
    private Long idUsuario;
    private Long idPublicacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
