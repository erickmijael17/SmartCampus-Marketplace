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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
                .cantidad(1)
                .precioUnitario(BigDecimal.valueOf(20))
                .estado("PENDIENTE")
                .build());

        assertThat(response.getId()).isEqualTo(55L);
        assertThat(response.getEstado()).isEqualTo("PENDIENTE");
    }
}
