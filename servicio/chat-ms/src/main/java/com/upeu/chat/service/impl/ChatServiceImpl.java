package com.upeu.chat.service.impl;

import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.entity.Conversacion;
import com.upeu.chat.entity.Mensaje;
import com.upeu.chat.repository.ConversacionRepository;
import com.upeu.chat.repository.MensajeRepository;
import com.upeu.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;

    @Override
    public List<ConversacionResponse> findAll() {
        return conversacionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConversacionResponse findById(Long id) {
        Conversacion entity = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado con ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    public ConversacionResponse save(ConversacionRequest request) {
        Conversacion entity = new Conversacion();
        entity.setIdUsuario1(request.getIdUsuario1());
        entity.setIdUsuario2(request.getIdUsuario2());
        Conversacion saved = conversacionRepository.save(entity);
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
                .findBetweenUsers(request.getIdComprador(), request.getIdVendedor())
                .orElseGet(() -> conversacionRepository.save(Conversacion.builder()
                        .idUsuario1(request.getIdComprador())
                        .idUsuario2(request.getIdVendedor())
                        .build()));

        Mensaje mensaje = Mensaje.builder()
                .idConversacion(conversacion.getId())
                .idRemitente(null)
                .contenido(buildComprobanteMessage(request))
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
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
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

    private String formatMonto(String moneda, BigDecimal monto) {
        String symbol = "PEN".equalsIgnoreCase(moneda) ? "S/" : valueOrDash(moneda);
        return symbol + " " + (monto == null ? "0.00" : monto.stripTrailingZeros().toPlainString());
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
