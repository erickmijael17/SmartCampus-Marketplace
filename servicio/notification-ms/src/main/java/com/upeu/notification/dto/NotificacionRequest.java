package com.upeu.notification.dto;

import lombok.Data;

@Data
public class NotificacionRequest {
    private Long idUsuario;
    private String titulo;
    private String mensaje;
    private Boolean leido;
}
