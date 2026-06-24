package com.upeu.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionResponse {
    private Long id;
    private Long idUsuario;
    private String titulo;
    private String mensaje;
    private Boolean leido;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
