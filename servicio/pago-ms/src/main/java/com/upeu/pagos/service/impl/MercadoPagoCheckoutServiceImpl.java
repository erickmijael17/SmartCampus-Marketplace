package com.upeu.pagos.service.impl;

import com.upeu.pagos.client.ChatClient;
import com.upeu.pagos.client.MercadoPagoClient;
import com.upeu.pagos.client.MercadoPagoPaymentResult;
import com.upeu.pagos.client.MercadoPagoPreferencePayload;
import com.upeu.pagos.client.MercadoPagoPreferenceResult;
import com.upeu.pagos.config.AppProperties;
import com.upeu.pagos.client.OrdenClient;
import com.upeu.pagos.config.MercadoPagoProperties;
import com.upeu.pagos.dto.ActualizarEstadoOrdenRequest;
import com.upeu.pagos.dto.ComprobantePagoRequest;
import com.upeu.pagos.dto.ComprobantePagoResponse;
import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.MensajeVentaValidadaRequest;
import com.upeu.pagos.dto.MensajeVentaValidadaResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.dto.ValidarTransaccionMercadoPagoResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.evento.EventoPago;
import com.upeu.pagos.exception.BadRequestException;
import com.upeu.pagos.exception.ConflictException;
import com.upeu.pagos.exception.MercadoPagoConsultaException;
import com.upeu.pagos.exception.ResourceNotFoundException;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import com.upeu.pagos.service.MercadoPagoCheckoutService;
import com.upeu.pagos.service.ProductorPago;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoCheckoutServiceImpl implements MercadoPagoCheckoutService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";
    private static final String ESTADO_CANCELADO = "CANCELADO";
    private static final String MONEDA_PEN = "PEN";

    private final PagoRepository pagoRepository;
    private final MercadoPagoClient mercadoPagoClient;
    private final MercadoPagoProperties properties;
    private final AppProperties appProperties;
    private final PagoMapper pagoMapper;
    private final OrdenClient ordenClient;
    private final ChatClient chatClient;
    private final ProductorPago productorPago;

    @Override
    @Transactional
    public MercadoPagoPreferenceResponse createPreference(MercadoPagoPreferenceRequest request) {
        String frontendUrl = resolveFrontendUrl();
        String successUrl = frontendUrl + "/pago/exito";
        String failureUrl = frontendUrl + "/pago/fallo";
        String pendingUrl = frontendUrl + "/pago/pendiente";
        log.info("MP back_urls success={}, failure={}, pending={}", successUrl, failureUrl, pendingUrl);

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
                        successUrl,
                        failureUrl,
                        pendingUrl
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
    public PagoConfirmacionResponse confirmarPago(String paymentId, String status, String externalReference) {
        MercadoPagoPaymentResult payment = mercadoPagoClient.getPayment(paymentId);
        String resolvedExternalReference = firstNonBlank(externalReference, payment.externalReference());
        Pago pago = findPagoForConfirmation(resolvedExternalReference);
        String resolvedPaymentId = firstNonBlank(payment.id(), paymentId);
        String resolvedStatus = firstNonBlank(payment.status(), status);

        boolean alreadyApproved = ESTADO_APROBADO.equals(pago.getEstado()) && resolvedPaymentId.equals(pago.getMpPaymentId());

        if (!alreadyApproved) {
            pago.setMpPaymentId(resolvedPaymentId);
            pago.setMpStatus(resolvedStatus);
            pago.setEstado(mapStatus(resolvedStatus));
            pago.setUpdatedAt(LocalDateTime.now());
            pago = pagoRepository.save(pago);
        }

        PagoConfirmacionResponse response = PagoConfirmacionResponse.builder()
                .pagoId(pago.getId())
                .ordenId(pago.getIdOrden())
                .estado(pago.getEstado())
                .estadoPago(pago.getEstado())
                .estadoOrden("PENDIENTE") // Valor por defecto
                .chatId(pago.getChatId())
                .conversacionId(pago.getChatId())
                .mensaje("Pago recibido desde Mercado Pago.")
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
                    pago.setChatId(chatResp.getChatId());
                    pago.setUpdatedAt(LocalDateTime.now());
                    pagoRepository.save(pago);
                    response.setChatId(pago.getChatId());
                    response.setConversacionId(pago.getChatId());
                    response.setMensaje("Pago confirmado y comprobante enviado al chat.");
                    response.setMensajeComprobanteEnviado(chatResp.isMensajeComprobanteEnviado());
                } else {
                    // Si ya estaba aprobado, no reenviamos al chat.
                    response.setMensaje("Pago ya confirmado previamente.");
                    response.setMensajeComprobanteEnviado(true);
                }
            } catch (Exception e) {
                log.error("Error al crear comprobante en chat-ms", e);
                response.setMensaje("Pago confirmado, pero no se pudo crear el comprobante en chat.");
                response.setMensajeComprobanteEnviado(false);
            }
        }
        
        return response;
    }

    @Override
    @Transactional
    public ValidarTransaccionMercadoPagoResponse validarTransaccion(Long pagoId, String paymentId) {
        String normalizedPaymentId = normalizePaymentId(paymentId);
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago con id " + pagoId + " no encontrado"));

        if (ESTADO_APROBADO.equals(pago.getEstado()) && normalizedPaymentId.equals(pago.getMpPaymentId())) {
            log.info(
                    "service=pago-ms component=payment-validation pagoId={} idOrden={} paymentId={} eventType=pago.aprobado status=already_published",
                    pago.getId(),
                    pago.getIdOrden(),
                    normalizedPaymentId
            );
            boolean chatMessageCreated = createValidatedSaleMessageSafely(pago);
            return buildValidationResponse(pago, approvedBuyerMessage(pago), chatMessageCreated);
        }
        if (ESTADO_APROBADO.equals(pago.getEstado())) {
            throw new ConflictException("El pago ya fue aprobado con otra transaccion");
        }

        Long currentPagoId = pago.getId();
        pagoRepository.findByMpPaymentId(normalizedPaymentId)
                .filter(existing -> !existing.getId().equals(currentPagoId))
                .ifPresent(existing -> {
                    throw new ConflictException("El numero de transaccion ya fue usado por otro pago");
                });

        MercadoPagoPaymentResult payment = fetchPayment(normalizedPaymentId);
        String mpStatus = firstNonBlank(payment.status(), "unknown");
        log.info(
                "MP validar transaccion pagoId={}, idOrden={}, paymentId={}, status={}, external_reference={}, transaction_amount={}",
                pago.getId(),
                pago.getIdOrden(),
                normalizedPaymentId,
                mpStatus,
                payment.externalReference(),
                payment.transactionAmount()
        );

        validateMercadoPagoPayment(pago, payment, normalizedPaymentId);

        String mappedStatus = mapStatus(mpStatus);
        pago.setMpPaymentId(firstNonBlank(payment.id(), normalizedPaymentId));
        pago.setMpStatus(mpStatus);
        pago.setEstado(mappedStatus);
        pago.setUpdatedAt(LocalDateTime.now());

        String mensaje = messageForStatus(mappedStatus);
        boolean chatMessageCreated = false;
        if (ESTADO_APROBADO.equals(mappedStatus)) {
            pago.setFechaConfirmacion(LocalDateTime.now());
            if (!pago.isEventoPagoAprobadoPublicado()) {
                productorPago.enviarEventoPago(EventoPago.builder()
                        .tipoEvento("pago.aprobado")
                        .pagoId(pago.getId())
                        .ordenId(pago.getIdOrden())
                        .idComprador(pago.getIdComprador())
                        .idVendedor(pago.getIdVendedor())
                        .publicacionId(pago.getPublicacionId())
                        .tituloProducto(pago.getTituloProducto())
                        .monto(pago.getMonto() == null ? null : pago.getMonto().doubleValue())
                        .moneda(pago.getMoneda())
                        .mpPaymentId(pago.getMpPaymentId())
                        .estado(ESTADO_APROBADO)
                        .origen("mercado-pago-validacion-manual")
                        .timestamp(System.currentTimeMillis())
                        .build());
                pago.setEventoPagoAprobadoPublicado(true);
            } else {
                log.info(
                        "service=pago-ms component=payment-validation pagoId={} idOrden={} paymentId={} eventType=pago.aprobado status=already_published",
                        pago.getId(),
                        pago.getIdOrden(),
                        pago.getMpPaymentId()
                );
            }
            chatMessageCreated = createValidatedSaleMessageSafely(pago);
            mensaje = approvedBuyerMessage(pago);
        }

        pago = pagoRepository.save(pago);
        return buildValidationResponse(pago, mensaje, chatMessageCreated);
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
        if ("cancelled".equalsIgnoreCase(mercadoPagoStatus) || "canceled".equalsIgnoreCase(mercadoPagoStatus)) {
            return ESTADO_CANCELADO;
        }
        return ESTADO_RECHAZADO;
    }

    private String normalizePaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new BadRequestException("paymentId es obligatorio");
        }
        String normalized = paymentId.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("paymentId es obligatorio");
        }
        return normalized;
    }

    private MercadoPagoPaymentResult fetchPayment(String paymentId) {
        try {
            MercadoPagoPaymentResult payment = mercadoPagoClient.obtenerPagoPorId(paymentId);
            if (payment == null) {
                throw new ResourceNotFoundException("Mercado Pago no encontro la transaccion " + paymentId);
            }
            return payment;
        } catch (RestClientResponseException e) {
            if (HttpStatus.NOT_FOUND.value() == e.getStatusCode().value()) {
                throw new ResourceNotFoundException("Mercado Pago no encontro la transaccion " + paymentId);
            }
            log.error(
                    "Error consultando Mercado Pago status={}, body={}, paymentId={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    paymentId
            );
            throw new MercadoPagoConsultaException("No se pudo consultar Mercado Pago. Intenta nuevamente.", e);
        }
    }

    private void validateMercadoPagoPayment(Pago pago, MercadoPagoPaymentResult payment, String requestedPaymentId) {
        String externalReference = payment.externalReference();
        if (externalReference == null || externalReference.isBlank()) {
            throw new ConflictException("Mercado Pago no devolvio external_reference para validar la orden");
        }
        String expectedExternalReference = firstNonBlank(pago.getExternalReference(), "ORDEN-" + pago.getIdOrden());
        if (!expectedExternalReference.equals(externalReference)) {
            throw new ConflictException("El numero de transaccion no corresponde a esta orden");
        }
        if (payment.transactionAmount() == null) {
            throw new BadRequestException("Mercado Pago no devolvio el monto de la transaccion");
        }
        if (pago.getMonto() == null || normalizeAmount(pago.getMonto()).compareTo(normalizeAmount(payment.transactionAmount())) != 0) {
            throw new BadRequestException("El monto de la transaccion no coincide con el pago local");
        }
        if (!MONEDA_PEN.equalsIgnoreCase(firstNonBlank(payment.currencyId(), ""))) {
            throw new BadRequestException("La moneda de la transaccion no coincide con PEN");
        }
        String resolvedPaymentId = firstNonBlank(payment.id(), requestedPaymentId);
        pagoRepository.findByMpPaymentId(resolvedPaymentId)
                .filter(existing -> !existing.getId().equals(pago.getId()))
                .ifPresent(existing -> {
                    throw new ConflictException("El numero de transaccion ya fue usado por otro pago");
                });
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String messageForStatus(String estado) {
        if (ESTADO_APROBADO.equals(estado)) {
            return "Pago validado correctamente";
        }
        if (ESTADO_PENDIENTE.equals(estado)) {
            return "El pago existe, pero aun no esta aprobado";
        }
        if (ESTADO_CANCELADO.equals(estado)) {
            return "El pago fue cancelado";
        }
        return "El pago fue rechazado";
    }

    private ValidarTransaccionMercadoPagoResponse buildValidationResponse(Pago pago, String mensaje, boolean chatMessageCreated) {
        return ValidarTransaccionMercadoPagoResponse.builder()
                .pagoId(pago.getId())
                .idOrden(pago.getIdOrden())
                .estado(pago.getEstado())
                .mercadoPagoPaymentId(pago.getMpPaymentId())
                .mpPaymentId(pago.getMpPaymentId())
                .tituloProducto(pago.getTituloProducto())
                .preferenceId(pago.getMpPreferenceId())
                .externalReference(pago.getExternalReference())
                .monto(pago.getMonto())
                .moneda(pago.getMoneda())
                .mensaje(mensaje)
                .chatMessageCreated(chatMessageCreated)
                .build();
    }

    private boolean createValidatedSaleMessageSafely(Pago pago) {
        try {
            MensajeVentaValidadaResponse response = chatClient.createValidatedSaleMessage(MensajeVentaValidadaRequest.builder()
                    .idComprador(pago.getIdComprador())
                    .idVendedor(pago.getIdVendedor())
                    .publicacionId(pago.getPublicacionId())
                    .idOrden(pago.getIdOrden())
                    .pagoId(pago.getId())
                    .tituloProducto(pago.getTituloProducto())
                    .monto(pago.getMonto())
                    .moneda(pago.getMoneda())
                    .mpPaymentId(pago.getMpPaymentId())
                    .build());
            if (response == null) {
                log.warn(
                        "chat-ms devolvio respuesta vacia al crear mensaje automatico de venta validada pagoId={}, ordenId={}",
                        pago.getId(),
                        pago.getIdOrden()
                );
                return false;
            }
            log.info(
                    "Mensaje automatico de venta validada creado en chat-ms pagoId={}, ordenId={}, chatId={}, mensajeId={}, creado={}",
                    pago.getId(),
                    pago.getIdOrden(),
                    response.getChatId(),
                    response.getMensajeId(),
                    response.isCreado()
            );
            return response.isCreado();
        } catch (Exception e) {
            log.error(
                    "No se pudo crear mensaje automatico de venta validada en chat-ms pagoId={}, ordenId={}, compradorId={}, vendedorId={}, publicacionId={}: {}",
                    pago.getId(),
                    pago.getIdOrden(),
                    pago.getIdComprador(),
                    pago.getIdVendedor(),
                    pago.getPublicacionId(),
                    e.getMessage()
            );
            return false;
        }
    }

    private String approvedBuyerMessage(Pago pago) {
        String titulo = firstNonBlank(pago.getTituloProducto(), "producto");
        String monto = pago.getMonto() == null ? "0.00" : normalizeAmount(pago.getMonto()).toPlainString();
        return "Su pago del producto " + titulo
                + " con numero de venta #" + pago.getIdOrden()
                + " por el precio de S/ " + monto
                + " ha sido validado correctamente. En seguida el vendedor se contactara con usted para coordinar la entrega.";
    }

    private String normalizeMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            return "MERCADO_PAGO";
        }
        return metodoPago.trim().toUpperCase();
    }

    private String resolveAutoReturn() {
        return properties.isAutoReturnEnabled() ? "approved" : null;
    }

    private Pago findPagoForConfirmation(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            throw new ResourceNotFoundException("Pago Mercado Pago sin external_reference");
        }
        return pagoRepository.findByExternalReference(externalReference)
                .or(() -> parseOrdenId(externalReference).flatMap(pagoRepository::findByIdOrden))
                .orElseThrow(() -> new ResourceNotFoundException("Pago Mercado Pago no encontrado para referencia " + externalReference));
    }

    private java.util.Optional<Long> parseOrdenId(String externalReference) {
        if (externalReference == null || !externalReference.startsWith("ORDEN-")) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Long.parseLong(externalReference.substring("ORDEN-".length())));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private String resolveFrontendUrl() {
        String frontendUrl = appProperties.getFrontendUrl();
        if (frontendUrl == null || frontendUrl.isBlank()) {
            throw new IllegalStateException("frontendUrl no configurado para Mercado Pago");
        }
        String normalized = frontendUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            throw new IllegalStateException("frontendUrl no configurado para Mercado Pago");
        }
        return normalized;
    }
}
