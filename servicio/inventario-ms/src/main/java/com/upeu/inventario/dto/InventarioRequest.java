package com.upeu.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class InventarioRequest {

    @NotNull(message = "idProducto es obligatorio")
    private Long idProducto;

    @NotNull(message = "stockDisponible es obligatorio")
    @Min(value = 0, message = "stockDisponible no puede ser negativo")
    private Integer stockDisponible;

    @NotNull(message = "stockReservado es obligatorio")
    @Min(value = 0, message = "stockReservado no puede ser negativo")
    private Integer stockReservado;

    @NotBlank(message = "estado es obligatorio")
    private String estado;
}

