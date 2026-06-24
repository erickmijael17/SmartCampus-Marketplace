package com.upeu.chat.dto;

import lombok.Data;

@Data
public class MensajeRequest {
    private Long idConversacion;
    private Long idRemitente;
    private String contenido;
    private Boolean leido;
}
