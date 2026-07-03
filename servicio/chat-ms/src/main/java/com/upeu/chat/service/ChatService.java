package com.upeu.chat.service;

import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.dto.EventoPago;
import com.upeu.chat.dto.EventoVentaConfirmada;
import com.upeu.chat.dto.MensajeVentaValidadaRequest;
import com.upeu.chat.dto.MensajeVentaValidadaResponse;
import java.util.List;

public interface ChatService {
    List<ConversacionResponse> findAll();
    List<ConversacionResponse> getMyChats(String token);
    List<ConversacionResponse> findByUsuario(Long usuarioId);
    ConversacionResponse findById(Long id);
    ConversacionResponse save(ConversacionRequest request);
    ConversacionResponse update(Long id, ConversacionRequest request);
    ComprobantePagoResponse crearComprobante(ComprobantePagoRequest request);
    ComprobantePagoResponse crearComprobantePagoAprobado(EventoPago evento);
    void crearMensajesVentaConfirmada(EventoVentaConfirmada evento);
    MensajeVentaValidadaResponse crearMensajeVentaValidada(MensajeVentaValidadaRequest request);
    void delete(Long id);
}
