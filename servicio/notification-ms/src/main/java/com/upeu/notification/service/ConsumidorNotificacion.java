package com.upeu.notification.service;

import com.upeu.notification.dto.NotificacionRequest;
import com.upeu.notification.evento.EventoOrden;
import com.upeu.notification.evento.EventoPago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorNotificacion {

    private static final String TIPO_ORDEN_CREADA = "orden.creada";
    private static final String TIPO_PAGO_APROBADO = "pago.aprobado";
    private static final String TIPO_PAGO_RECHAZADO = "pago.rechazado";

    private final NotificacionService notificacionService;

    @Value("${app.kafka.topic.ordenes:ordenes-topic}")
    private String topicOrdenes;

    @Value("${app.kafka.topic.pagos:pagos-topic}")
    private String topicPagos;

    @KafkaListener(
            topics = "${app.kafka.topic.ordenes:ordenes-topic}",
            groupId = "${app.kafka.group-id.notifications:notifications-group}",
            containerFactory = "ordenKafkaListenerContainerFactory"
    )
    public void consumirEventoOrden(EventoOrden evento) {
        if (evento == null || !TIPO_ORDEN_CREADA.equals(evento.getTipoEvento())) {
            return;
        }

        Long userId = evento.getIdComprador();
        if (userId == null) {
            log.warn("service=notification-ms event=orden.creada ordenId={} status=skipped reason=no-comprador", evento.getOrdenId());
            return;
        }

        NotificacionRequest request = new NotificacionRequest();
        request.setIdUsuario(userId);
        request.setTitulo("Orden registrada");
        request.setMensaje("Tu orden #" + evento.getOrdenId() + " fue creada correctamente.");
        request.setLeido(false);

        notificacionService.create(request);
        log.info("service=notification-ms topic={} ordenId={} userId={} status=notified", topicOrdenes, evento.getOrdenId(), userId);
    }

    @KafkaListener(
            topics = "${app.kafka.topic.pagos:pagos-topic}",
            groupId = "${app.kafka.group-id.notifications:notifications-group}",
            containerFactory = "pagoKafkaListenerContainerFactory"
    )
    public void consumirEventoPago(EventoPago evento) {
        if (evento == null || evento.getOrdenId() == null) {
            return;
        }

        if (!TIPO_PAGO_APROBADO.equals(evento.getTipoEvento()) && !TIPO_PAGO_RECHAZADO.equals(evento.getTipoEvento())) {
            return;
        }

        NotificacionRequest request = new NotificacionRequest();
        request.setIdUsuario(1L);
        request.setTitulo(TIPO_PAGO_APROBADO.equals(evento.getTipoEvento()) ? "Pago aprobado" : "Pago rechazado");
        request.setMensaje(
                "El pago de la orden #" + evento.getOrdenId() + " fue " + evento.getEstado() + "."
        );
        request.setLeido(false);

        notificacionService.create(request);
        log.info("service=notification-ms topic={} ordenId={} estado={} status=notified", topicPagos, evento.getOrdenId(), evento.getEstado());
    }
}
