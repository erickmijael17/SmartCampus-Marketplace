package com.upeu.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversacionResponse {
    private Long id;
    private Long idUsuario1;
    private Long idUsuario2;
    private Long publicacionId;
    private Long idOrden;
    private String tipoChat;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    private String nombreUsuario1;
    private String nombreUsuario2;
    private String ultimoMensaje;
    private String tipoUltimoMensaje;
    private LocalDateTime ultimoMensajeFecha;
}
