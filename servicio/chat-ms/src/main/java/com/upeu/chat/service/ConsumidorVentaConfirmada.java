package com.upeu.chat.service;

import com.upeu.chat.dto.EventoVentaConfirmada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorVentaConfirmada {

    private static final String TIPO_EVENTO_VENTA_CONFIRMADA = "venta.confirmada";

    private final ChatService chatService;

    @KafkaListener(
            topics = "${app.kafka.topic.ordenes:ordenes-topic}",
            groupId = "${app.kafka.group-id.chat:chat-group}",
            containerFactory = "ventaConfirmadaKafkaListenerContainerFactory"
    )
    public void consumir(EventoVentaConfirmada evento) {
        if (evento == null || !TIPO_EVENTO_VENTA_CONFIRMADA.equals(evento.getEventType())) {
            log.debug("service=chat-ms component=sale-consumer eventType={} status=ignored", evento != null ? evento.getEventType() : null);
            return;
        }

        log.info(
                "evento venta.confirmada consumido ordenId={} pagoId={}",
                evento.getOrdenId(),
                evento.getPagoId()
        );
        chatService.crearMensajesVentaConfirmada(evento);
    }
}
