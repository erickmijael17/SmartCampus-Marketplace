package com.upeu.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProductoRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 120, message = "El titulo no debe superar los 120 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripcion no debe superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(max = 10, message = "La moneda no debe superar los 10 caracteres")
    private String moneda;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 20, message = "El estado no debe superar los 20 caracteres")
    private String estado;

    @NotNull(message = "El idCategoria es obligatorio")
    private Long idCategoria;

    @NotNull(message = "El idVendedor es obligatorio")
    private Long idVendedor;
}
