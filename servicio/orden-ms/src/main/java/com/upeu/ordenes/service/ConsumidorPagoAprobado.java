package com.upeu.ordenes.service;

import com.upeu.ordenes.evento.EventoPagoAprobado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorPagoAprobado {

    private static final String TIPO_EVENTO_PAGO_APROBADO = "pago.aprobado";

    private final OrdenService ordenService;

    @KafkaListener(
            topics = "${app.kafka.topic.pagos:pagos-topic}",
            groupId = "${app.kafka.group-id.orden:orden-ms-group}",
            containerFactory = "pagoAprobadoKafkaListenerContainerFactory"
    )
    public void consumir(EventoPagoAprobado evento) {
        if (evento == null || !TIPO_EVENTO_PAGO_APROBADO.equals(evento.getTipoEvento())) {
            log.debug("service=orden-ms component=payment-consumer eventType={} status=ignored", evento != null ? evento.getTipoEvento() : null);
            return;
        }

        log.info(
                "evento pago.aprobado consumido ordenId={} pagoId={}",
                evento.getOrdenId(),
                evento.getPagoId()
        );
        ordenService.confirmarVentaDesdePago(evento);
    }
}
