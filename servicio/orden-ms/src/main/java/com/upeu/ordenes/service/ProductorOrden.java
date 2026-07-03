package com.upeu.ordenes.service;

import com.upeu.ordenes.evento.EventoOrden;
import com.upeu.ordenes.evento.EventoVentaConfirmada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductorOrden {

    private final KafkaTemplate<String, EventoOrden> kafkaTemplate;
    private final KafkaTemplate<String, EventoVentaConfirmada> ventaConfirmadaKafkaTemplate;

    @Value("${app.kafka.topic.ordenes}")
    private String topicOrdenes;

    public ProductorOrden(
            @Qualifier("ordenKafkaTemplate") KafkaTemplate<String, EventoOrden> kafkaTemplate,
            @Qualifier("ventaConfirmadaKafkaTemplate") KafkaTemplate<String, EventoVentaConfirmada> ventaConfirmadaKafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.ventaConfirmadaKafkaTemplate = ventaConfirmadaKafkaTemplate;
    }

    public void publicarOrdenCreada(EventoOrden eventoOrden) {
        kafkaTemplate.send(topicOrdenes, String.valueOf(eventoOrden.getOrdenId()), eventoOrden)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error(
                                "service=orden-ms component=producer topic={} eventType={} ordenId={} timestamp={} status=error error=\"{}\"",
                                topicOrdenes,
                                eventoOrden.getTipoEvento(),
                                eventoOrden.getOrdenId(),
                                eventoOrden.getTimestamp(),
                                ex.getMessage()
                        );
                        return;
                    }

                    log.info(
                            "service=orden-ms component=producer topic={} partition={} offset={} eventType={} ordenId={} timestamp={} status=published",
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            eventoOrden.getTipoEvento(),
                            eventoOrden.getOrdenId(),
                            eventoOrden.getTimestamp()
                    );
                });
    }

    public void publicarVentaConfirmada(EventoVentaConfirmada evento) {
        ventaConfirmadaKafkaTemplate.send(topicOrdenes, String.valueOf(evento.getOrdenId()), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error(
                                "service=orden-ms component=producer topic={} eventType={} ordenId={} timestamp={} status=error error=\"{}\"",
                                topicOrdenes,
                                evento.getEventType(),
                                evento.getOrdenId(),
                                evento.getTimestamp(),
                                ex.getMessage()
                        );
                        return;
                    }

                    log.info(
                            "service=orden-ms component=producer topic={} partition={} offset={} eventType={} ordenId={} timestamp={} status=published",
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            evento.getEventType(),
                            evento.getOrdenId(),
                            evento.getTimestamp()
                    );
                    log.info(
                            "evento venta.confirmada publicado ordenId={} pagoId={} vendedorId={} compradorId={}",
                            evento.getOrdenId(),
                            evento.getPagoId(),
                            evento.getVendedorId(),
                            evento.getCompradorId()
                    );
                });
    }
}
