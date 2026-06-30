package com.upeu.pagos.service;

import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.PagoResponse;

public interface MercadoPagoCheckoutService {

    MercadoPagoPreferenceResponse createPreference(MercadoPagoPreferenceRequest request);

    PagoResponse syncPayment(String paymentId);
}
