package com.upeu.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensajeResponse {
    private Long id;
    private Long idConversacion;
    private Long idRemitente;
    private String contenido;
    private String tipoRemitente;
    private String tipoMensaje;
    private Long idOrden;
    private Long pagoId;
    private String mpPaymentId;
    private Boolean leido;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
