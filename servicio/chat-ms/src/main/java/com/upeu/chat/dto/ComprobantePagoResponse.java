package com.upeu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePagoResponse {
    private Long chatId;
    private Long mensajeId;
    private boolean mensajeComprobanteEnviado;
}
