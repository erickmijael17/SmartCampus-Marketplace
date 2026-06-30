package com.upeu.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceResponse {

    private Long pagoId;
    private Long idOrden;
    private String estado;
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
}
