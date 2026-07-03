package com.upeu.pagos.service.impl;

import com.upeu.pagos.dto.PagoRequest;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.dto.VendedorVentasResumenResponse;
import com.upeu.pagos.entity.Pago;
import com.upeu.pagos.exception.ResourceNotFoundException;
import com.upeu.pagos.mapper.PagoMapper;
import com.upeu.pagos.repository.PagoRepository;
import com.upeu.pagos.service.PagoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String ESTADO_APROBADO = "APROBADO";

    private final PagoRepository pagosRepository;
    private final PagoMapper pagosMapper;

    @Override
    @Transactional
    public PagoResponse create(PagoRequest request) {
        Pago pagos = pagosMapper.toEntity(request);
        return pagosMapper.toResponse(pagosRepository.save(pagos));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> findAll() {
        return pagosRepository.findAll().stream().map(pagosMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> findByComprador(Long idComprador) {
        return pagosRepository.findByIdComprador(idComprador).stream().map(pagosMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse findById(Long id) {
        return pagosMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorVentasResumenResponse getResumenVentasVendedor(Long idVendedor) {
        long ventas = pagosRepository.countByIdVendedorAndEstado(idVendedor, ESTADO_APROBADO);
        BigDecimal montoTotal = pagosRepository.sumMontoByIdVendedorAndEstado(idVendedor, ESTADO_APROBADO);
        return VendedorVentasResumenResponse.builder()
                .idVendedor(idVendedor)
                .ventas(ventas)
                .montoTotal(montoTotal == null ? BigDecimal.ZERO : montoTotal)
                .build();
    }

    @Override
    @Transactional
    public PagoResponse update(Long id, PagoRequest request) {
        Pago entity = getEntity(id);
        pagosMapper.updateEntity(entity, request);
        return pagosMapper.toResponse(pagosRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        pagosRepository.deleteById(id);
    }

    private Pago getEntity(Long id) {
        return pagosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago con id " + id + " no encontrado"));
    }
}

