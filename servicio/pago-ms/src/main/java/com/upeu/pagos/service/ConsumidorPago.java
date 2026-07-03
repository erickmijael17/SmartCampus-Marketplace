package com.upeu.pagos.service;

import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.evento.EventoOrden;
import com.upeu.pagos.evento.EventoPago;
import com.upeu.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorPago {

    private static final String TIPO_EVENTO_ORDEN_CREADA = "orden.creada";
    private static final String TIPO_EVENTO_PAGO_APROBADO = "pago.aprobado";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String METODO_MERCADO_PAGO = "MERCADO_PAGO";

    private final PagoRepository pagoRepository;
    private final ProductorPago productorPago;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${app.kafka.topic.ordenes}")
    private String topicOrdenes;
    @Value("${app.kafka.group-id.pagos}")
    private String groupIdPagos;

    @KafkaListener(
            topics = "${app.kafka.topic.ordenes}",
            groupId = "${app.kafka.group-id.pagos}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirEventoOrden(EventoOrden eventoOrden) {
        if (eventoOrden == null || !TIPO_EVENTO_ORDEN_CREADA.equals(eventoOrden.getTipoEvento())) {
            log.warn("service=pago-ms component=consumer eventType={} status=ignored", eventoOrden != null ? eventoOrden.getTipoEvento() : null);
            return;
        }

        long processedAt = Instant.now().toEpochMilli();
        Long latencyMs = eventoOrden.getTimestamp() != null ? processedAt - eventoOrden.getTimestamp() : null;
        String metodoPago = normalizeMetodoPago(eventoOrden.getMetodoPago());

        log.info(
                "service=pago-ms component=consumer topic={} groupId={} eventType={} ordenId={} metodoPago={} timestamp={} processedAt={} latencyMs={} status=consumed",
                topicOrdenes,
                groupIdPagos,
                eventoOrden.getTipoEvento(),
                eventoOrden.getOrdenId(),
                metodoPago,
                eventoOrden.getTimestamp(),
                processedAt,
                latencyMs
        );

        if (METODO_MERCADO_PAGO.equals(metodoPago) || hasPendingMercadoPago(eventoOrden.getOrdenId())) {
            createPendingMercadoPagoIfMissing(eventoOrden, METODO_MERCADO_PAGO);
            log.info(
                    "service=pago-ms component=processor ordenId={} metodoPago={} accion=skip_auto_approval status=pending_waiting_external_confirmation",
                    eventoOrden.getOrdenId(),
                    metodoPago
            );
            return;
        }

        EventoPago eventoPago = EventoPago.builder()
                .tipoEvento(TIPO_EVENTO_PAGO_APROBADO)
                .ordenId(eventoOrden.getOrdenId())
                .monto(eventoOrden.getTotal())
                .estado(ESTADO_APROBADO)
                .origen(applicationName)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        productorPago.enviarEventoPago(eventoPago);

        log.info(
                "service=pago-ms component=processor ordenId={} metodoPago={} accion=publish_auto_approval estadoPago={} status=processed",
                eventoOrden.getOrdenId(),
                metodoPago,
                ESTADO_APROBADO
        );
    }

    private boolean hasPendingMercadoPago(Long ordenId) {
        if (ordenId == null) {
            return false;
        }
        return pagoRepository.findByIdOrden(ordenId)
                .filter(pago -> METODO_MERCADO_PAGO.equals(normalizeMetodoPago(pago.getMetodoPago())))
                .filter(pago -> ESTADO_PENDIENTE.equalsIgnoreCase(pago.getEstado()))
                .isPresent();
    }

    private void createPendingMercadoPagoIfMissing(EventoOrden eventoOrden, String metodoPago) {
        if (eventoOrden.getOrdenId() == null || pagoRepository.findByIdOrden(eventoOrden.getOrdenId()).isPresent()) {
            return;
        }
        Pago pago = Pago.builder()
                .idOrden(eventoOrden.getOrdenId())
                .idComprador(eventoOrden.getIdComprador() == null ? 0L : eventoOrden.getIdComprador())
                .monto(eventoOrden.getTotal() == null ? BigDecimal.ZERO : BigDecimal.valueOf(eventoOrden.getTotal()))
                .moneda("PEN")
                .metodoPago(metodoPago)
                .estado(ESTADO_PENDIENTE)
                .referenciaTransaccion("ORDEN-" + eventoOrden.getOrdenId())
                .externalReference("ORDEN-" + eventoOrden.getOrdenId())
                .build();
        pagoRepository.save(pago);
    }

    private String normalizeMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            return "NO_INFORMADO";
        }
        return metodoPago.trim().toUpperCase();
    }
}
