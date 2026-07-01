package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.ChatClient;
import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.client.OrdenClient;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.ActualizarEstadoOrdenRequest;
import com.upeu.pagos.dto.ComprobantePagoRequest;
import com.upeu.pagos.dto.ComprobantePagoResponse;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoCheckoutServiceImpl implements MercadoPagoCheckoutService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String MONEDA_PEN = "PEN";

    private final PagoRepository pagoRepository;
    private final MercadoPagoClient mercadoPagoClient;
    private final MercadoPagoProperties properties;
    private final PagoMapper pagoMapper;
    private final OrdenClient ordenClient;
    private final ChatClient chatClient;

    @Override
    @Transactional
    public MercadoPagoPreferenceResponse createPreference(MercadoPagoPreferenceRequest request) {
        BigDecimal monto = request.getPrecioUnitario().multiply(BigDecimal.valueOf(request.getCantidad()));
        Long idComprador = request.getIdComprador() == null ? 0L : request.getIdComprador();
        Pago pago = Pago.builder()
                .idOrden(request.getIdOrden())
                .idComprador(idComprador)
                .idVendedor(request.getIdVendedor())
                .publicacionId(request.getIdProducto())
                .tituloProducto(request.getTitulo())
                .descripcionProducto(request.getDescripcion())
                .monto(monto)
                .moneda(MONEDA_PEN)
                .metodoPago(normalizeMetodoPago(request.getMetodoPago()))
                .estado(ESTADO_PENDIENTE)
                .referenciaTransaccion("MP-PENDING")
                .build();

        pago = pagoRepository.save(pago);
        String externalReference = "ORDEN-" + request.getIdOrden();
        pago.setExternalReference(externalReference);
        pago.setReferenciaTransaccion(externalReference);
        pago = pagoRepository.save(pago);

        MercadoPagoPreferencePayload payload = new MercadoPagoPreferencePayload(
                List.of(new MercadoPagoPreferencePayload.Item(
                        request.getTitulo(),
                        request.getDescripcion(),
                        request.getCantidad(),
                        MONEDA_PEN,
                        request.getPrecioUnitario()
                )),
                new MercadoPagoPreferencePayload.BackUrls(
                        properties.getSuccessUrl(),
                        properties.getFailureUrl(),
                        properties.getPendingUrl()
                ),
                properties.getNotificationUrl(),
                externalReference,
                resolveAutoReturn()
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
                preference.sandboxInitPoint(),
                externalReference
        );
    }

    @Override
    @Transactional
    public PagoResponse syncPayment(String paymentId) {
        return pagoMapper.toResponse(processPaymentConfirmation(paymentId));
    }

    @Override
    @Transactional
    public PagoConfirmacionResponse confirmarPago(String paymentId) {
        MercadoPagoPaymentResult payment = mercadoPagoClient.getPayment(paymentId);
        Pago pago = pagoRepository.findByExternalReference(payment.externalReference())
                .orElseThrow(() -> new ResourceNotFoundException("Pago Mercado Pago no encontrado para referencia " + payment.externalReference()));

        boolean alreadyApproved = ESTADO_APROBADO.equals(pago.getEstado()) && payment.id().equals(pago.getMpPaymentId());

        if (!alreadyApproved) {
            pago.setMpPaymentId(payment.id());
            pago.setMpStatus(payment.status());
            pago.setEstado(mapStatus(payment.status()));
            pago.setUpdatedAt(LocalDateTime.now());
            pago = pagoRepository.save(pago);
        }

        PagoConfirmacionResponse response = PagoConfirmacionResponse.builder()
                .ordenId(pago.getIdOrden())
                .estadoPago(pago.getEstado())
                .estadoOrden("PENDIENTE") // Valor por defecto
                .mensajeComprobanteEnviado(false)
                .build();

        if (ESTADO_APROBADO.equals(pago.getEstado())) {
            try {
                if (!alreadyApproved) {
                    ordenClient.updateEstado(pago.getIdOrden(), new ActualizarEstadoOrdenRequest("PAGADA"));
                }
                response.setEstadoOrden("PAGADA");
            } catch (Exception e) {
                log.error("Error al actualizar la orden en orden-ms", e);
                response.setEstadoOrden("ERROR_ACTUALIZACION");
            }

            try {
                if (!alreadyApproved) {
                    ComprobantePagoRequest chatReq = ComprobantePagoRequest.builder()
                            .ordenId(pago.getIdOrden())
                            .idComprador(pago.getIdComprador())
                            .idVendedor(pago.getIdVendedor())
                            .publicacionId(pago.getPublicacionId())
                            .tituloProducto(pago.getTituloProducto())
                            .monto(pago.getMonto())
                            .moneda(pago.getMoneda())
                            .estadoPago(pago.getEstado())
                            .metodoPago(pago.getMetodoPago())
                            .paymentId(pago.getMpPaymentId())
                            .fechaPago(LocalDateTime.now())
                            .build();

                    ComprobantePagoResponse chatResp = chatClient.createReceipt(chatReq);
                    response.setChatId(chatResp.getChatId());
                    response.setMensajeComprobanteEnviado(chatResp.isMensajeComprobanteEnviado());
                } else {
                    // Si ya estaba aprobado, no reenviamos al chat.
                    response.setMensajeComprobanteEnviado(true);
                }
            } catch (Exception e) {
                log.error("Error al crear comprobante en chat-ms", e);
                response.setMensajeComprobanteEnviado(false);
            }
        }
        
        return response;
    }

    private Pago processPaymentConfirmation(String paymentId) {
        MercadoPagoPaymentResult payment = mercadoPagoClient.getPayment(paymentId);
        Pago pago = pagoRepository.findByExternalReference(payment.externalReference())
                .orElseThrow(() -> new ResourceNotFoundException("Pago Mercado Pago no encontrado para referencia " + payment.externalReference()));

        if (ESTADO_APROBADO.equals(pago.getEstado()) && payment.id().equals(pago.getMpPaymentId())) {
            return pago;
        }

        pago.setMpPaymentId(payment.id());
        pago.setMpStatus(payment.status());
        pago.setEstado(mapStatus(payment.status()));
        pago.setUpdatedAt(LocalDateTime.now());
        return pagoRepository.save(pago);
    }

    private String mapStatus(String mercadoPagoStatus) {
        if ("approved".equalsIgnoreCase(mercadoPagoStatus)) {
            return ESTADO_APROBADO;
        }
        if ("pending".equalsIgnoreCase(mercadoPagoStatus) || "in_process".equalsIgnoreCase(mercadoPagoStatus)) {
            return ESTADO_PENDIENTE;
        }
        return "RECHAZADO";
    }

    private String normalizeMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            return "MERCADO_PAGO";
        }
        return metodoPago.trim().toUpperCase();
    }

    private String resolveAutoReturn() {
        String successUrl = properties.getSuccessUrl();
        if (successUrl == null || successUrl.isBlank()) {
            return null;
        }
        String normalizedUrl = successUrl.trim().toLowerCase();
        if (!normalizedUrl.startsWith("https://")) {
            return null;
        }
        return "approved";
    }
}
