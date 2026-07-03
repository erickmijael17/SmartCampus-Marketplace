package com.upeu.chat.dto;

import lombok.Data;

@Data
public class ConversacionRequest {
    private Long idUsuario1;
    private Long idUsuario2;
    private Long publicacionId;
    private Long idOrden;
    private String tipoChat;
}
