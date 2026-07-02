package com.upeu.pagos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidarTransaccionMercadoPagoRequest {

    @NotBlank(message = "paymentId es obligatorio")
    @JsonAlias("numeroTransaccion")
    private String paymentId;
}
