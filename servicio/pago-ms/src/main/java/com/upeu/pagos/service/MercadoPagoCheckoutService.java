package com.upeu.pagos.service;

import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.dto.ValidarTransaccionMercadoPagoResponse;

public interface MercadoPagoCheckoutService {

    MercadoPagoPreferenceResponse createPreference(MercadoPagoPreferenceRequest request);

    PagoResponse syncPayment(String paymentId);

    PagoConfirmacionResponse confirmarPago(String paymentId, String status, String externalReference);

    ValidarTransaccionMercadoPagoResponse validarTransaccion(Long pagoId, String paymentId);
}
