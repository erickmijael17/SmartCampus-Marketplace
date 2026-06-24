package com.upeu.favoritos.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoritoResponse {
    private Long id;
    private Long idUsuario;
    private Long idPublicacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
