package com.upeu.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversacionResponse {
    private Long id;
    private Long idUsuario1;
    private Long idUsuario2;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
