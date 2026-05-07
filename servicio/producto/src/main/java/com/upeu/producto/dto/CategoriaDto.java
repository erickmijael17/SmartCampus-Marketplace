package com.upeu.producto.dto;
import lombok.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
