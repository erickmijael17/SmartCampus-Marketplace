package com.upeu.pagos.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class PagoRequest {

    @NotNull(message = "idComprador es obligatorio")
    private Long idComprador;

    @NotNull(message = "idOrden es obligatorio")
    private Long idOrden;

    @NotNull(message = "monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "metodoPago es obligatorio")
    private String metodoPago;

    @NotBlank(message = "estado es obligatorio")
    private String estado;

    private String referenciaTransaccion;
}

