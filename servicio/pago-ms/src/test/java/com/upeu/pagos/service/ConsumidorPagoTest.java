package com.upeu.pagos.service;

import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.evento.EventoOrden;
import com.upeu.pagos.evento.EventoPago;
import com.upeu.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumidorPagoTest {

    @Test
    void mercadoPagoOrderCreatedDoesNotPublishApprovedPayment() {
        PagoRepository repository = mock(PagoRepository.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        ConsumidorPago consumidor = new ConsumidorPago(repository, productorPago);
        when(repository.findByIdOrden(90L)).thenReturn(Optional.empty());
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        consumidor.consumirEventoOrden(EventoOrden.builder()
                .tipoEvento("orden.creada")
                .ordenId(90L)
                .idComprador(8L)
                .total(100.0)
                .metodoPago("MERCADO_PAGO")
                .estado("PENDIENTE")
                .timestamp(System.currentTimeMillis())
                .build());

        verify(productorPago, never()).enviarEventoPago(any(EventoPago.class));
        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(repository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo("PENDIENTE");
        assertThat(pagoCaptor.getValue().getMetodoPago()).isEqualTo("MERCADO_PAGO");
        assertThat(pagoCaptor.getValue().getMonto()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
    }

    @Test
    void simulatedOrderCreatedCanPublishApprovedPayment() {
        PagoRepository repository = mock(PagoRepository.class);
        ProductorPago productorPago = mock(ProductorPago.class);
        ConsumidorPago consumidor = new ConsumidorPago(repository, productorPago);

        consumidor.consumirEventoOrden(EventoOrden.builder()
                .tipoEvento("orden.creada")
                .ordenId(91L)
                .idComprador(8L)
                .total(50.0)
                .metodoPago("TRANSFERENCIA_SIMULADA")
                .estado("PENDIENTE")
                .timestamp(System.currentTimeMillis())
                .build());

        ArgumentCaptor<EventoPago> eventoCaptor = ArgumentCaptor.forClass(EventoPago.class);
        verify(productorPago).enviarEventoPago(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo("pago.aprobado");
        assertThat(eventoCaptor.getValue().getEstado()).isEqualTo("APROBADO");
        assertThat(eventoCaptor.getValue().getOrdenId()).isEqualTo(91L);
    }
}
