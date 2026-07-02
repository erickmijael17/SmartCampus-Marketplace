package com.upeu.ordenes.service.impl;

import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.entity.Orden;
import com.upeu.ordenes.evento.EventoOrden;
import com.upeu.ordenes.mapper.OrdenMapper;
import com.upeu.ordenes.repository.OrdenRepository;
import com.upeu.ordenes.service.ProductorOrden;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdenServiceImplTest {

    @Test
    void createsOrderEvenWhenKafkaPublishFails() {
        OrdenRepository repository = mock(OrdenRepository.class);
        ProductorOrden productorOrden = mock(ProductorOrden.class);
        OrdenServiceImpl service = new OrdenServiceImpl(repository, new OrdenMapper(), productorOrden);
        when(repository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(55L);
            return orden;
        });
        doThrow(new RuntimeException("Kafka no disponible"))
                .when(productorOrden)
                .publicarOrdenCreada(any(EventoOrden.class));

        OrdenResponse response = service.create(OrdenRequest.builder()
                .idComprador(8L)
                .idProducto(99L)
                .idVendedor(12L)
                .cantidad(1)
                .precioUnitario(BigDecimal.valueOf(20))
                .estado("PENDIENTE")
                .build());

        assertThat(response.getId()).isEqualTo(55L);
        assertThat(response.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void publishesMetodoPagoInOrderCreatedEvent() {
        OrdenRepository repository = mock(OrdenRepository.class);
        ProductorOrden productorOrden = mock(ProductorOrden.class);
        OrdenServiceImpl service = new OrdenServiceImpl(repository, new OrdenMapper(), productorOrden);
        when(repository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(55L);
            return orden;
        });

        service.create(OrdenRequest.builder()
                .idComprador(8L)
                .idProducto(99L)
                .idVendedor(12L)
                .cantidad(1)
                .precioUnitario(BigDecimal.valueOf(20))
                .estado("PENDIENTE")
                .metodoPago("MERCADO_PAGO")
                .build());

        ArgumentCaptor<EventoOrden> eventoCaptor = ArgumentCaptor.forClass(EventoOrden.class);
        verify(productorOrden).publicarOrdenCreada(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getMetodoPago()).isEqualTo("MERCADO_PAGO");
        assertThat(eventoCaptor.getValue().getIdVendedor()).isEqualTo(12L);
    }

    @Test
    void rejectsOwnProductPurchase() {
        OrdenServiceImpl service = new OrdenServiceImpl(mock(OrdenRepository.class), new OrdenMapper(), mock(ProductorOrden.class));

        assertThatThrownBy(() -> service.create(OrdenRequest.builder()
                .idComprador(8L)
                .idVendedor(8L)
                .idProducto(99L)
                .cantidad(1)
                .precioUnitario(BigDecimal.valueOf(20))
                .estado("PENDIENTE")
                .metodoPago("MERCADO_PAGO")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No puedes comprar tu propio producto.");
    }
}
