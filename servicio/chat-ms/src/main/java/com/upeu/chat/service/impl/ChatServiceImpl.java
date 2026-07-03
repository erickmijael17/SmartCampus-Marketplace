package com.upeu.chat.service.impl;

import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.dto.EventoPago;
import com.upeu.chat.dto.EventoVentaConfirmada;
import com.upeu.chat.dto.MensajeVentaValidadaRequest;
import com.upeu.chat.dto.MensajeVentaValidadaResponse;
import com.upeu.chat.entity.Conversacion;
import com.upeu.chat.entity.Mensaje;
import com.upeu.chat.repository.ConversacionRepository;
import com.upeu.chat.repository.MensajeRepository;
import com.upeu.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String TIPO_CHAT_VENTA = "VENTA";
    private static final String TIPO_REMITENTE_SISTEMA = "SISTEMA";
    private static final String TIPO_MENSAJE_CONFIRMACION_PAGO = "SISTEMA_CONFIRMACION_PAGO";
    private static final String TIPO_MENSAJE_VENTA_VALIDADA = "VENTA_VALIDADA";
    private static final String TIPO_MENSAJE_VENTA_CONFIRMADA_VENDEDOR = "VENTA_CONFIRMADA_VENDEDOR";
    private static final String TIPO_MENSAJE_VENTA_CONFIRMADA_COMPRADOR = "VENTA_CONFIRMADA_COMPRADOR";

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final com.upeu.chat.client.AuthClient authClient;

    @Override
    public List<ConversacionResponse> findAll() {
        return conversacionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversacionResponse> findByUsuario(Long usuarioId) {
        return conversacionRepository.findByUsuario(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversacionResponse> getMyChats(String token) {
        Long usuarioId = getLocalUserId(token);
        return findByUsuario(usuarioId);
    }

    @SuppressWarnings("unchecked")
    private Long getLocalUserId(String token) {
        try {
            java.util.Map<String, Object> userInfo = authClient.getUserInfo("Bearer " + token);
            Object localIdObj = userInfo.get("localId");
            if (localIdObj instanceof Number) {
                return ((Number) localIdObj).longValue();
            }
            throw new RuntimeException("No se pudo obtener el localId del usuario");
        } catch (Exception e) {
            log.error("Error al obtener informacion del usuario desde auth-ms", e);
            throw new RuntimeException("Error de autenticacion: no se puede verificar el usuario", e);
        }
    }

    @Override
    public ConversacionResponse findById(Long id) {
        Conversacion entity = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado con ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    public ConversacionResponse save(ConversacionRequest request) {
        Conversacion saved = findConversationForSale(request.getPublicacionId(), request.getIdUsuario1(), request.getIdUsuario2())
                .orElseGet(() -> {
                    Conversacion entity = new Conversacion();
                    entity.setIdUsuario1(request.getIdUsuario1());
                    entity.setIdUsuario2(request.getIdUsuario2());
                    entity.setPublicacionId(request.getPublicacionId());
                    entity.setIdOrden(request.getIdOrden());
                    entity.setTipoChat(request.getTipoChat());
                    return conversacionRepository.save(entity);
                });
        return mapToResponse(saved);
    }

    @Override
    public ConversacionResponse update(Long id, ConversacionRequest request) {
        Conversacion entity = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado con ID: " + id));
        entity.setIdUsuario1(request.getIdUsuario1());
        entity.setIdUsuario2(request.getIdUsuario2());
        Conversacion saved = conversacionRepository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComprobantePagoResponse crearComprobante(ComprobantePagoRequest request) {
        Conversacion conversacion = conversacionRepository
                .findByIdOrden(request.getOrdenId())
                .or(() -> findConversationForSale(request.getPublicacionId(), request.getIdComprador(), request.getIdVendedor()))
                .orElseGet(() -> conversacionRepository.save(Conversacion.builder()
                        .idUsuario1(request.getIdComprador())
                        .idUsuario2(request.getIdVendedor())
                        .publicacionId(request.getPublicacionId())
                        .idOrden(request.getOrdenId())
                        .tipoChat(TIPO_CHAT_VENTA)
                        .build()));

        Mensaje mensaje = Mensaje.builder()
                .idConversacion(conversacion.getId())
                .idRemitente(null)
                .contenido(buildComprobanteMessage(request))
                .tipoRemitente(TIPO_REMITENTE_SISTEMA)
                .tipoMensaje(TIPO_MENSAJE_CONFIRMACION_PAGO)
                .idOrden(request.getOrdenId())
                .pagoId(null)
                .mpPaymentId(request.getPaymentId())
                .leido(Boolean.FALSE)
                .build();
        Mensaje saved = mensajeRepository.save(mensaje);

        return ComprobantePagoResponse.builder()
                .chatId(conversacion.getId())
                .mensajeId(saved.getId())
                .mensajeComprobanteEnviado(true)
                .build();
    }

    @Override
    @Transactional
    public ComprobantePagoResponse crearComprobantePagoAprobado(EventoPago evento) {
        java.util.Optional<Conversacion> existente = findConversationForSale(
                evento.getPublicacionId(),
                evento.getIdComprador(),
                evento.getIdVendedor()
        ).or(() -> conversacionRepository.findByIdOrden(evento.getOrdenId()));

        Conversacion conversacion = existente.orElseGet(() -> {
            Conversacion nueva = conversacionRepository.save(Conversacion.builder()
                    .idUsuario1(evento.getIdComprador())
                    .idUsuario2(evento.getIdVendedor())
                    .publicacionId(evento.getPublicacionId())
                    .idOrden(evento.getOrdenId())
                    .tipoChat(TIPO_CHAT_VENTA)
                    .build());
            log.info(
                    "chat-ms chat creado chatId={}, ordenId={}, compradorId={}, vendedorId={}, publicacionId={}",
                    nueva.getId(),
                    evento.getOrdenId(),
                    evento.getIdComprador(),
                    evento.getIdVendedor(),
                    evento.getPublicacionId()
            );
            return nueva;
        });

        if (existente.isPresent()) {
            log.info(
                    "chat-ms chat encontrado chatId={}, ordenId={}, compradorId={}, vendedorId={}, publicacionId={}",
                    conversacion.getId(),
                    evento.getOrdenId(),
                    evento.getIdComprador(),
                    evento.getIdVendedor(),
                    evento.getPublicacionId()
            );
        }

        if (conversacion.getPublicacionId() == null && evento.getPublicacionId() != null) {
            conversacion.setPublicacionId(evento.getPublicacionId());
            conversacion.setTipoChat(TIPO_CHAT_VENTA);
            conversacionRepository.save(conversacion);
        }

        if (mensajeRepository.existsByIdConversacionAndIdOrdenAndTipoMensaje(
                conversacion.getId(),
                evento.getOrdenId(),
                TIPO_MENSAJE_CONFIRMACION_PAGO
        )) {
            log.info(
                    "mensaje sistema ya existe para ordenId={}",
                    evento.getOrdenId()
            );
            return ComprobantePagoResponse.builder()
                    .chatId(conversacion.getId())
                    .mensajeComprobanteEnviado(false)
                    .build();
        }

        Mensaje mensaje = Mensaje.builder()
                .idConversacion(conversacion.getId())
                .idRemitente(null)
                .contenido(buildPagoAprobadoMessage(evento))
                .tipoRemitente(TIPO_REMITENTE_SISTEMA)
                .tipoMensaje(TIPO_MENSAJE_CONFIRMACION_PAGO)
                .idOrden(evento.getOrdenId())
                .pagoId(evento.getPagoId())
                .mpPaymentId(evento.getMpPaymentId())
                .leido(Boolean.FALSE)
                .build();
        Mensaje saved = mensajeRepository.save(mensaje);
        log.info(
                "chat-ms mensaje sistema insertado mensajeId={}, chatId={}, ordenId={}, pagoId={}, mpPaymentId={}",
                saved.getId(),
                conversacion.getId(),
                evento.getOrdenId(),
                evento.getPagoId(),
                evento.getMpPaymentId()
        );

        return ComprobantePagoResponse.builder()
                .chatId(conversacion.getId())
                .mensajeId(saved.getId())
                .mensajeComprobanteEnviado(true)
                .build();
    }

    @Override
    @Transactional
    public void crearMensajesVentaConfirmada(EventoVentaConfirmada evento) {
        Long publicacionId = resolvePublicacionId(evento);
        java.util.Optional<Conversacion> existente = conversacionRepository.findByIdOrden(evento.getOrdenId())
                .or(() -> findConversationForSale(publicacionId, evento.getCompradorId(), evento.getVendedorId()));

        Conversacion conversacion = existente.orElseGet(() -> {
            Conversacion nueva = conversacionRepository.save(Conversacion.builder()
                    .idUsuario1(evento.getCompradorId())
                    .idUsuario2(evento.getVendedorId())
                    .publicacionId(publicacionId)
                    .idOrden(evento.getOrdenId())
                    .tipoChat(TIPO_CHAT_VENTA)
                    .build());
            log.info(
                    "conversacion creada/reutilizada chatId={} ordenId={} compradorId={} vendedorId={}",
                    nueva.getId(),
                    evento.getOrdenId(),
                    evento.getCompradorId(),
                    evento.getVendedorId()
            );
            return nueva;
        });

        if (existente.isPresent()) {
            boolean changed = false;
            if (conversacion.getIdOrden() == null && evento.getOrdenId() != null) {
                conversacion.setIdOrden(evento.getOrdenId());
                changed = true;
            }
            if (conversacion.getPublicacionId() == null && publicacionId != null) {
                conversacion.setPublicacionId(publicacionId);
                changed = true;
            }
            if (conversacion.getTipoChat() == null) {
                conversacion.setTipoChat(TIPO_CHAT_VENTA);
                changed = true;
            }
            if (changed) {
                conversacionRepository.save(conversacion);
            }
            log.info(
                    "conversacion creada/reutilizada chatId={} ordenId={} compradorId={} vendedorId={}",
                    conversacion.getId(),
                    evento.getOrdenId(),
                    evento.getCompradorId(),
                    evento.getVendedorId()
            );
        }

        crearMensajeVentaConfirmadaSiFalta(
                conversacion,
                evento,
                TIPO_MENSAJE_VENTA_CONFIRMADA_VENDEDOR,
                evento.getVendedorId(),
                "Tu producto " + valueOrDash(evento.getTituloProducto())
                        + " ha sido vendido. Comunícate de inmediato con el comprador para coordinar la entrega."
        );
        crearMensajeVentaConfirmadaSiFalta(
                conversacion,
                evento,
                TIPO_MENSAJE_VENTA_CONFIRMADA_COMPRADOR,
                evento.getCompradorId(),
                "Tu compra de " + valueOrDash(evento.getTituloProducto())
                        + " fue aprobada. Puedes comunicarte con el vendedor para coordinar la entrega."
        );
    }

    @Override
    @Transactional
    public MensajeVentaValidadaResponse crearMensajeVentaValidada(MensajeVentaValidadaRequest request) {
        java.util.Optional<Conversacion> existente = findConversationForSale(
                request.getPublicacionId(),
                request.getIdComprador(),
                request.getIdVendedor()
        );

        Conversacion conversacion = existente.orElseGet(() -> {
            Conversacion nueva = conversacionRepository.save(Conversacion.builder()
                    .idUsuario1(request.getIdComprador())
                    .idUsuario2(request.getIdVendedor())
                    .publicacionId(request.getPublicacionId())
                    .tipoChat(TIPO_CHAT_VENTA)
                    .build());
            log.info(
                    "chat-ms chat venta-validada creado chatId={}, ordenId={}, compradorId={}, vendedorId={}, publicacionId={}",
                    nueva.getId(),
                    request.getIdOrden(),
                    request.getIdComprador(),
                    request.getIdVendedor(),
                    request.getPublicacionId()
            );
            return nueva;
        });

        if (existente.isPresent()) {
            log.info(
                    "chat-ms chat venta-validada encontrado chatId={}, ordenId={}, compradorId={}, vendedorId={}, publicacionId={}",
                    conversacion.getId(),
                    request.getIdOrden(),
                    request.getIdComprador(),
                    request.getIdVendedor(),
                    request.getPublicacionId()
            );
        }

        java.util.Optional<Mensaje> duplicado = mensajeRepository
                .findFirstByIdConversacionAndIdOrdenAndPagoIdAndMpPaymentIdAndTipoMensaje(
                        conversacion.getId(),
                        request.getIdOrden(),
                        request.getPagoId(),
                        request.getMpPaymentId(),
                        TIPO_MENSAJE_VENTA_VALIDADA
                );
        if (duplicado.isPresent()) {
            log.info(
                    "mensaje venta validada ya existe para ordenId={}, pagoId={}, mpPaymentId={}",
                    request.getIdOrden(),
                    request.getPagoId(),
                    request.getMpPaymentId()
            );
            return MensajeVentaValidadaResponse.builder()
                    .chatId(conversacion.getId())
                    .mensajeId(duplicado.get().getId())
                    .mensaje("Mensaje de venta validada ya existia")
                    .creado(false)
                    .build();
        }

        String contenido = buildVentaValidadaMessage(request);
        Mensaje mensaje = Mensaje.builder()
                .idConversacion(conversacion.getId())
                .idRemitente(request.getIdComprador())
                .contenido(contenido)
                .tipoRemitente("USUARIO")
                .tipoMensaje(TIPO_MENSAJE_VENTA_VALIDADA)
                .idOrden(request.getIdOrden())
                .pagoId(request.getPagoId())
                .mpPaymentId(request.getMpPaymentId())
                .leido(Boolean.FALSE)
                .build();
        Mensaje saved = mensajeRepository.save(mensaje);
        log.info(
                "chat-ms mensaje venta validada insertado mensajeId={}, chatId={}, ordenId={}, pagoId={}, compradorId={}, vendedorId={}",
                saved.getId(),
                conversacion.getId(),
                request.getIdOrden(),
                request.getPagoId(),
                request.getIdComprador(),
                request.getIdVendedor()
        );

        return MensajeVentaValidadaResponse.builder()
                .chatId(conversacion.getId())
                .mensajeId(saved.getId())
                .mensaje(contenido)
                .creado(true)
                .build();
    }

    @Override
    public void delete(Long id) {
        Conversacion entity = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado con ID: " + id));
        conversacionRepository.delete(entity);
    }

    private ConversacionResponse mapToResponse(Conversacion entity) {
        ConversacionResponse response = new ConversacionResponse();
        response.setId(entity.getId());
        response.setIdUsuario1(entity.getIdUsuario1());
        response.setIdUsuario2(entity.getIdUsuario2());
        response.setPublicacionId(entity.getPublicacionId());
        response.setIdOrden(entity.getIdOrden());
        response.setTipoChat(entity.getTipoChat());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());

        response.setNombreUsuario1(resolveName(entity.getIdUsuario1()));
        response.setNombreUsuario2(resolveName(entity.getIdUsuario2()));
        
        // Find last message
        mensajeRepository.findByIdConversacionOrderByCreadoEnAsc(entity.getId()).stream()
                .reduce((first, second) -> second)
                .ifPresent(lastMsg -> {
                    response.setUltimoMensaje(lastMsg.getContenido());
                    response.setTipoUltimoMensaje(lastMsg.getTipoMensaje());
                    response.setUltimoMensajeFecha(lastMsg.getCreadoEn());
                });

        return response;
    }

    private String resolveName(Long userId) {
        if (userId == null) return null;
        try {
            java.util.Map<String, Object> profile = authClient.getPublicProfile(userId);
            if (profile != null && profile.get("nombre") != null) {
                String nombre = (String) profile.get("nombre");
                String apellido = (String) profile.getOrDefault("apellido", "");
                return (nombre + " " + apellido).trim();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el nombre para userId {}", userId);
        }
        return "Usuario #" + userId;
    }

    private String buildComprobanteMessage(ComprobantePagoRequest request) {
        return String.join("\n",
                "Pago confirmado",
                "",
                "Producto: " + valueOrDash(request.getTituloProducto()),
                "Orden: #" + request.getOrdenId(),
                "Monto: " + formatMonto(request.getMoneda(), request.getMonto()),
                "Estado: " + valueOrDash(request.getEstadoPago()),
                "Metodo: " + valueOrDash(request.getMetodoPago()),
                "Referencia: " + valueOrDash(request.getPaymentId()),
                "",
                "El comprador realizo el pago correctamente."
        );
    }

    private String buildPagoAprobadoMessage(EventoPago evento) {
        String comprador = valueOrDefault(evento.getNombreComprador(), null);
        String sujeto = comprador == null
                ? "El comprador adquirio"
                : "El usuario " + comprador + " compro";
        return "Compra confirmada. " + sujeto
                + " el producto \"" + valueOrDash(evento.getTituloProducto())
                + "\" con numero de venta #" + evento.getOrdenId()
                + " por " + formatMonto(evento.getMoneda(), BigDecimal.valueOf(evento.getMonto() == null ? 0.0 : evento.getMonto()))
                + ". El pago fue validado correctamente. Coordinen la entrega por este chat.";
    }

    private String buildVentaValidadaMessage(MensajeVentaValidadaRequest request) {
        return "Hola, compr\u00e9 tu producto \u201c" + valueOrDash(request.getTituloProducto())
                + "\u201d con n\u00famero de venta #" + request.getIdOrden()
                + " por " + formatMonto(request.getMoneda(), request.getMonto())
                + ". Mi pago ya fue validado correctamente. Podemos coordinar la entrega por este chat.";
    }

    private String formatMonto(String moneda, BigDecimal monto) {
        String symbol = "PEN".equalsIgnoreCase(moneda) ? "S/" : valueOrDash(moneda);
        return symbol + " " + (monto == null ? "0.00" : monto.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private java.util.Optional<Conversacion> findConversationForSale(Long publicacionId, Long compradorId, Long vendedorId) {
        if (publicacionId != null) {
            return conversacionRepository.findByPublicacionAndUsers(publicacionId, compradorId, vendedorId)
                    .or(() -> conversacionRepository.findBetweenUsers(compradorId, vendedorId));
        }
        return conversacionRepository.findBetweenUsers(compradorId, vendedorId);
    }

    private Long resolvePublicacionId(EventoVentaConfirmada evento) {
        if (evento.getPublicacionId() != null) {
            return evento.getPublicacionId();
        }
        return evento.getProductoId();
    }

    private void crearMensajeVentaConfirmadaSiFalta(
            Conversacion conversacion,
            EventoVentaConfirmada evento,
            String tipoMensaje,
            Long receptorId,
            String contenido
    ) {
        if (mensajeRepository.existsByIdConversacionAndIdOrdenAndPagoIdAndTipoMensaje(
                conversacion.getId(),
                evento.getOrdenId(),
                evento.getPagoId(),
                tipoMensaje
        )) {
            log.info(
                    "evento duplicado ignorado ordenId={} pagoId={} tipoMensaje={}",
                    evento.getOrdenId(),
                    evento.getPagoId(),
                    tipoMensaje
            );
            return;
        }

        Mensaje saved = mensajeRepository.save(Mensaje.builder()
                .idConversacion(conversacion.getId())
                .idRemitente(null)
                .receptorId(receptorId)
                .contenido(contenido)
                .tipoRemitente(TIPO_REMITENTE_SISTEMA)
                .tipoMensaje(tipoMensaje)
                .idOrden(evento.getOrdenId())
                .pagoId(evento.getPagoId())
                .leido(Boolean.FALSE)
                .build());

        if (TIPO_MENSAJE_VENTA_CONFIRMADA_VENDEDOR.equals(tipoMensaje)) {
            log.info(
                    "mensaje sistema vendedor creado vendedorId={} mensajeId={} chatId={} ordenId={} pagoId={}",
                    receptorId,
                    saved.getId(),
                    conversacion.getId(),
                    evento.getOrdenId(),
                    evento.getPagoId()
            );
            return;
        }

        log.info(
                "mensaje sistema comprador creado compradorId={} mensajeId={} chatId={} ordenId={} pagoId={}",
                receptorId,
                saved.getId(),
                conversacion.getId(),
                evento.getOrdenId(),
                evento.getPagoId()
        );
    }
}
