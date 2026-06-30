package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.exception.ResourceNotFoundException;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import com.upeu.pagos.service.MercadoPagoCheckoutService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MercadoPagoCheckoutServiceImpl implements MercadoPagoCheckoutService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final PagoRepository pagoRepository;
    private final MercadoPagoClient mercadoPagoClient;
    private final MercadoPagoProperties properties;
    private final PagoMapper pagoMapper;

    @Override
    @Transactional
    public MercadoPagoPreferenceResponse createPreference(MercadoPagoPreferenceRequest request) {
        BigDecimal monto = request.getPrecioUnitario().multiply(BigDecimal.valueOf(request.getCantidad()));
        Pago pago = Pago.builder()
                .idOrden(request.getIdOrden())
                .idComprador(request.getIdComprador())
                .monto(monto)
                .metodoPago(normalizeMetodoPago(request.getMetodoPago()))
                .estado(ESTADO_PENDIENTE)
                .referenciaTransaccion("MP-PENDING")
                .build();

        pago = pagoRepository.save(pago);
        String externalReference = "SCM-ORDEN-" + request.getIdOrden() + "-PAGO-" + pago.getId();
        pago.setExternalReference(externalReference);
        pago.setReferenciaTransaccion(externalReference);
        pago = pagoRepository.save(pago);

        MercadoPagoPreferencePayload payload = new MercadoPagoPreferencePayload(
                List.of(new MercadoPagoPreferencePayload.Item(
                        request.getTitulo(),
                        request.getDescripcion(),
                        request.getCantidad(),
                        "PEN",
                        request.getPrecioUnitario()
                )),
                new MercadoPagoPreferencePayload.BackUrls(
                        properties.getSuccessUrl(),
                        properties.getFailureUrl(),
                        properties.getPendingUrl()
                ),
                properties.getNotificationUrl(),
                externalReference
        );
        MercadoPagoPreferenceResult preference = mercadoPagoClient.createPreference(payload);

        pago.setMpPreferenceId(preference.id());
        pago.setCheckoutUrl(preference.sandboxInitPoint() != null ? preference.sandboxInitPoint() : preference.initPoint());
        pago.setUpdatedAt(LocalDateTime.now());
        pagoRepository.save(pago);

        return new MercadoPagoPreferenceResponse(
                pago.getId(),
                pago.getIdOrden(),
                pago.getEstado(),
                preference.id(),
                preference.initPoint(),
                preference.sandboxInitPoint()
        );
    }

    @Override
    @Transactional
    public PagoResponse syncPayment(String paymentId) {
        MercadoPagoPaymentResult payment = mercadoPagoClient.getPayment(paymentId);
        Pago pago = pagoRepository.findByExternalReference(payment.externalReference())
                .orElseThrow(() -> new ResourceNotFoundException("Pago Mercado Pago no encontrado para referencia " + payment.externalReference()));

        pago.setMpPaymentId(payment.id());
        pago.setMpStatus(payment.status());
        pago.setEstado(mapStatus(payment.status()));
        pago.setUpdatedAt(LocalDateTime.now());
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    private String mapStatus(String mercadoPagoStatus) {
        if ("approved".equalsIgnoreCase(mercadoPagoStatus)) {
            return "APROBADO";
        }
        if ("pending".equalsIgnoreCase(mercadoPagoStatus) || "in_process".equalsIgnoreCase(mercadoPagoStatus)) {
            return "PENDIENTE";
        }
        return "RECHAZADO";
    }

    private String normalizeMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            return "MERCADO_PAGO";
        }
        return metodoPago.trim().toUpperCase();
    }
}
