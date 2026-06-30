package com.upeu.ordenes.service.impl;

import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.entity.Orden;
import com.upeu.ordenes.evento.EventoOrden;
import com.upeu.ordenes.exception.ResourceNotFoundException;
import com.upeu.ordenes.mapper.OrdenMapper;
import com.upeu.ordenes.repository.OrdenRepository;
import com.upeu.ordenes.service.OrdenService;
import com.upeu.ordenes.service.ProductorOrden;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenesRepository;
    private final OrdenMapper ordenesMapper;
    private final ProductorOrden productorOrden;

    @Value("${spring.application.name:orden-ms}")
    private String applicationName;

    @Override
    @Transactional
    public OrdenResponse create(OrdenRequest request) {
        Orden ordenes = ordenesMapper.toEntity(request);
        ordenes.setEstado("PENDIENTE");
        Orden ordenGuardada = ordenesRepository.save(ordenes);

        double total = ordenGuardada.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(ordenGuardada.getCantidad()))
                .doubleValue();

        EventoOrden evento = EventoOrden.builder()
                .tipoEvento("orden.creada")
                .ordenId(ordenGuardada.getId())
                .idComprador(ordenGuardada.getIdComprador())
                .total(total)
                .estado(ordenGuardada.getEstado())
                .origen(applicationName)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        try {
            productorOrden.publicarOrdenCreada(evento);
        } catch (RuntimeException ex) {
            log.warn(
                    "service=orden-ms component=order-service ordenId={} status=created eventStatus=not_published error=\"{}\"",
                    ordenGuardada.getId(),
                    ex.getMessage()
            );
        }

        return ordenesMapper.toResponse(ordenGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> findAll() {
        return ordenesRepository.findAll().stream().map(ordenesMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> findByComprador(Long idComprador) {
        return ordenesRepository.findByIdComprador(idComprador).stream().map(ordenesMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponse findById(Long id) {
        return ordenesMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public OrdenResponse update(Long id, OrdenRequest request) {
        Orden entity = getEntity(id);
        ordenesMapper.updateEntity(entity, request);
        return ordenesMapper.toResponse(ordenesRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        ordenesRepository.deleteById(id);
    }

    private Orden getEntity(Long id) {
        return ordenesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden con id " + id + " no encontrado"));
    }
}

