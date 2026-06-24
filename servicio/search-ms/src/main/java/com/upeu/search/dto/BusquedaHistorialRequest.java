package com.upeu.search.dto;

import lombok.Data;

@Data
public class BusquedaHistorialRequest {
    private Long idUsuario;
    private String termino;
}
