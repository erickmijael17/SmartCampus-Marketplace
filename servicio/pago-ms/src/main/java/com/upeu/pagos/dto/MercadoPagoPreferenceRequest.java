package com.upeu.pagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceRequest {

    @NotNull(message = "idOrden es obligatorio")
    private Long idOrden;

    @NotNull(message = "idComprador es obligatorio")
    private Long idComprador;

    @NotNull(message = "idProducto es obligatorio")
    private Long idProducto;

    @NotBlank(message = "titulo es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "precioUnitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "precioUnitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    private String metodoPago;
}
