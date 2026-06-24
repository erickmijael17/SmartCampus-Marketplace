package com.upeu.calificacion.dto;

import lombok.Data;

@Data
public class CalificacionRequest {
    private Integer puntuacion;
    private String comentario;
    private Long idUsuario;
    private Long idPublicacion;
}
