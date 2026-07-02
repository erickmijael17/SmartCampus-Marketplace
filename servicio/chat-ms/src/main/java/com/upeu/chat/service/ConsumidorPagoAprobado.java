package com.upeu.chat.service;

import com.upeu.chat.dto.EventoPago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorPagoAprobado {

    private static final String TIPO_EVENTO_PAGO_APROBADO = "pago.aprobado";

    private final ChatService chatService;

    @KafkaListener(
            topics = "${app.kafka.topic.pagos:pagos-topic}",
            groupId = "${app.kafka.group-id.chat:chat-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(EventoPago evento) {
        if (evento == null || !TIPO_EVENTO_PAGO_APROBADO.equals(evento.getTipoEvento())) {
            log.debug("service=chat-ms component=payment-consumer eventType={} status=ignored", evento != null ? evento.getTipoEvento() : null);
            return;
        }
        log.info(
                "chat-ms received pago.aprobado ordenId={}, compradorId={}, vendedorId={}, publicacionId={}",
                evento.getOrdenId(),
                evento.getIdComprador(),
                evento.getIdVendedor(),
                evento.getPublicacionId()
        );
        log.info(
                "service=chat-ms component=payment-consumer eventType={} ordenId={} compradorId={} vendedorId={} publicacionId={} status=consumed",
                evento.getTipoEvento(),
                evento.getOrdenId(),
                evento.getIdComprador(),
                evento.getIdVendedor(),
                evento.getPublicacionId()
        );
        chatService.crearComprobantePagoAprobado(evento);
    }
}
