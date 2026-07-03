package com.upeu.ordenes.service.impl;

import com.upeu.ordenes.dto.ActualizarEstadoRequest;
import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.entity.Orden;
import com.upeu.ordenes.evento.EventoPagoAprobado;
import com.upeu.ordenes.evento.EventoOrden;
import com.upeu.ordenes.evento.EventoVentaConfirmada;
import com.upeu.ordenes.exception.ResourceNotFoundException;
import com.upeu.ordenes.mapper.OrdenMapper;
import com.upeu.ordenes.repository.OrdenRepository;
import com.upeu.ordenes.service.OrdenService;
import com.upeu.ordenes.service.ProductorOrden;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
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

    private static final String ESTADO_PAGADA = "PAGADA";
    private static final String ESTADO_VENDIDA = "VENDIDA";
    private static final String EVENT_TYPE_VENTA_CONFIRMADA = "venta.confirmada";
    private static final String ESTADO_PAGO_APROBADO = "APROBADO";

    private final OrdenRepository ordenesRepository;
    private final OrdenMapper ordenesMapper;
    private final ProductorOrden productorOrden;

    @Value("${spring.application.name:orden-ms}")
    private String applicationName;

    @Override
    @Transactional
    public OrdenResponse create(OrdenRequest request) {
        if (request.getIdVendedor() != null && request.getIdComprador().equals(request.getIdVendedor())) {
            throw new IllegalArgumentException("No puedes comprar tu propio producto.");
        }
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
                .idVendedor(ordenGuardada.getIdVendedor())
                .total(total)
                .estado(ordenGuardada.getEstado())
                .metodoPago(ordenGuardada.getMetodoPago())
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
    public OrdenResponse updateEstado(Long id, ActualizarEstadoRequest request) {
        Orden entity = getEntity(id);
        entity.setEstado(request.getEstado());
        return ordenesMapper.toResponse(ordenesRepository.save(entity));
    }

    @Override
    @Transactional
    public void confirmarVentaDesdePago(EventoPagoAprobado evento) {
        Orden orden = getEntity(evento.getOrdenId());

        if (orden.isVentaConfirmadaPublicada()) {
            log.info(
                    "evento duplicado ignorado ordenId={} pagoId={}",
                    evento.getOrdenId(),
                    evento.getPagoId()
            );
            return;
        }

        orden.setEstado(ESTADO_PAGADA);
        orden.setPagoId(evento.getPagoId());
        orden.setFechaVenta(LocalDateTime.now());

        EventoVentaConfirmada ventaConfirmada = EventoVentaConfirmada.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_VENTA_CONFIRMADA)
                .ordenId(orden.getId())
                .pagoId(evento.getPagoId())
                .productoId(resolveProductoId(evento, orden))
                .publicacionId(resolvePublicacionId(evento, orden))
                .tituloProducto(evento.getTituloProducto())
                .compradorId(orden.getIdComprador())
                .vendedorId(resolveVendedorId(evento, orden))
                .precio(resolvePrecio(evento, orden))
                .moneda(resolveMoneda(evento))
                .estadoPago(resolveEstadoPago(evento))
                .timestamp(LocalDateTime.now().toString())
                .build();

        productorOrden.publicarVentaConfirmada(ventaConfirmada);
        orden.setVentaConfirmadaPublicada(true);
        ordenesRepository.save(orden);

        log.info(
                "venta confirmada ordenId={} vendedorId={} compradorId={}",
                orden.getId(),
                ventaConfirmada.getVendedorId(),
                ventaConfirmada.getCompradorId()
        );
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

    private Long resolveProductoId(EventoPagoAprobado evento, Orden orden) {
        if (evento.getProductoId() != null) {
            return evento.getProductoId();
        }
        if (evento.getPublicacionId() != null) {
            return evento.getPublicacionId();
        }
        return orden.getIdProducto();
    }

    private Long resolvePublicacionId(EventoPagoAprobado evento, Orden orden) {
        if (evento.getPublicacionId() != null) {
            return evento.getPublicacionId();
        }
        if (evento.getProductoId() != null) {
            return evento.getProductoId();
        }
        return orden.getIdProducto();
    }

    private Long resolveVendedorId(EventoPagoAprobado evento, Orden orden) {
        return evento.getIdVendedor() != null ? evento.getIdVendedor() : orden.getIdVendedor();
    }

    private BigDecimal resolvePrecio(EventoPagoAprobado evento, Orden orden) {
        if (evento.getMonto() != null) {
            return BigDecimal.valueOf(evento.getMonto());
        }
        return orden.getPrecioUnitario();
    }

    private String resolveMoneda(EventoPagoAprobado evento) {
        return evento.getMoneda() == null || evento.getMoneda().isBlank() ? "PEN" : evento.getMoneda();
    }

    private String resolveEstadoPago(EventoPagoAprobado evento) {
        return evento.getEstadoPago() == null || evento.getEstadoPago().isBlank()
                ? ESTADO_PAGO_APROBADO
                : evento.getEstadoPago();
    }
}
