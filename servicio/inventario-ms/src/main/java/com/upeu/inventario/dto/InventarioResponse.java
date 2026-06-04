package com.upeu.inventario.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponse {

    private Long id;
    private Long idProducto;
    private Integer stockDisponible;
    private Integer stockReservado;
    private String estado;
    private LocalDateTime updatedAt;
}

