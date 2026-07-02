package com.upeu.pagos.controller;

import com.upeu.pagos.dto.MercadoPagoPreferenceRequest;
import com.upeu.pagos.dto.MercadoPagoPreferenceResponse;
import com.upeu.pagos.dto.PagoConfirmacionResponse;
import com.upeu.pagos.service.MercadoPagoCheckoutService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoCheckoutService checkoutService;

    @PostMapping("/preference")
    public ResponseEntity<?> createPreference(
            @Valid @RequestBody MercadoPagoPreferenceRequest request
    ) {
        if (request.getIdVendedor() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "idVendedor es obligatorio para generar comprobante de pago en chat"));
        }
        return ResponseEntity.ok(checkoutService.createPreference(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam Map<String, String> params
    ) {
        String paymentId = extractPaymentId(body, params);
        if (paymentId != null && !paymentId.isBlank()) {
            checkoutService.confirmarPago(paymentId, null, null);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/return")
    public ResponseEntity<Void> paymentReturn(@RequestParam Map<String, String> params) {
        String paymentId = params.get("payment_id");
        if (paymentId != null && !paymentId.isBlank()) {
            checkoutService.syncPayment(paymentId);
        }
        String status = params.getOrDefault("status", params.getOrDefault("collection_status", "pending"));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/payment-result?status=" + status))
                .build();
    }

    @GetMapping("/confirmar")
    public ResponseEntity<PagoConfirmacionResponse> confirmarPago(@RequestParam Map<String, String> params) {
        String paymentId = firstNonBlank(params.get("payment_id"), params.get("collection_id"));
        if (paymentId == null || paymentId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String status = firstNonBlank(params.get("status"), params.get("collection_status"));
        String externalReference = params.get("external_reference");
        return ResponseEntity.ok(checkoutService.confirmarPago(paymentId, status, externalReference));
    }

    @SuppressWarnings("unchecked")
    private String extractPaymentId(Map<String, Object> body, Map<String, String> params) {
        if (body != null) {
            Object data = body.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object id = dataMap.get("id");
                if (id != null) {
                    return String.valueOf(id);
                }
            }
            Object id = body.get("id");
            if (id != null) {
                return String.valueOf(id);
            }
        }
        return firstNonBlank(params.get("data.id"), params.get("id"));
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
}
