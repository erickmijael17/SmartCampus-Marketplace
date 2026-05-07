package com.upeu.catalogo.dto;

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
public class CategoriaResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
