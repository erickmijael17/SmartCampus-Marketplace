package com.upeu.carrito.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class CarritoRequest {

    @NotNull(message = "idComprador es obligatorio")
    private Long idComprador;

    @NotNull(message = "idProducto es obligatorio")
    private Long idProducto;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "cantidad debe ser mayor o igual a 1")
    private Integer cantidad;

    @NotNull(message = "precioUnitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "precioUnitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    @NotBlank(message = "estado es obligatorio")
    private String estado;
}
