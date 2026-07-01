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
    private static final String TIPO_EVENTO_PAGO_RECHAZADO = "pago.rechazado";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";

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

        log.info(
                "service=pago-ms component=consumer topic={} groupId={} eventType={} ordenId={} timestamp={} processedAt={} latencyMs={} status=consumed",
                topicOrdenes,
                groupIdPagos,
                eventoOrden.getTipoEvento(),
                eventoOrden.getOrdenId(),
                eventoOrden.getTimestamp(),
                processedAt,
                latencyMs
        );

        boolean pagoAprobado = Math.random() > 0.3;
        String estadoPago = pagoAprobado ? ESTADO_APROBADO : ESTADO_RECHAZADO;
        String tipoEventoPago = pagoAprobado ? TIPO_EVENTO_PAGO_APROBADO : TIPO_EVENTO_PAGO_RECHAZADO;

        // TEMPORAL_MOCK: Ya no creamos ni guardamos un Pago aquí porque MercadoPagoCheckoutServiceImpl
        // se encarga de crear el Pago real. Evitamos duplicados y errores de constraints.
        
        EventoPago eventoPago = EventoPago.builder()
                .tipoEvento(tipoEventoPago)
                .ordenId(eventoOrden.getOrdenId())
                .monto(eventoOrden.getTotal())
                .estado(estadoPago)
                .origen(applicationName)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        productorPago.enviarEventoPago(eventoPago);

        log.info(
                "service=pago-ms component=processor ordenId={} estadoPago={} status=processed",
                eventoOrden.getOrdenId(),
                estadoPago
        );
    }
}
