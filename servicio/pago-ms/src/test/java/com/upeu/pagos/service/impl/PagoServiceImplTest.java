package com.upeu.pagos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.upeu.pagos.dto.VendedorVentasResumenResponse;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PagoServiceImplTest {

    @Test
    void sellerSalesSummaryCountsOnlyApprovedPayments() {
        PagoRepository repository = mock(PagoRepository.class);
        PagoServiceImpl service = new PagoServiceImpl(repository, new PagoMapper());
        when(repository.countByIdVendedorAndEstado(4L, "APROBADO")).thenReturn(3L);
        when(repository.sumMontoByIdVendedorAndEstado(4L, "APROBADO")).thenReturn(BigDecimal.valueOf(240).setScale(2));

        VendedorVentasResumenResponse response = service.getResumenVentasVendedor(4L);

        assertThat(response.getIdVendedor()).isEqualTo(4L);
        assertThat(response.getVentas()).isEqualTo(3);
        assertThat(response.getMontoTotal()).isEqualByComparingTo("240.00");
    }
}
