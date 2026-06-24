package com.upeu.search.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BusquedaHistorialResponse {
    private Long id;
    private Long idUsuario;
    private String termino;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
