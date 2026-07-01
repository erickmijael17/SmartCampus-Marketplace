package com.upeu.chat.service;

import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import java.util.List;

public interface ChatService {
    List<ConversacionResponse> findAll();
    ConversacionResponse findById(Long id);
    ConversacionResponse save(ConversacionRequest request);
    ConversacionResponse update(Long id, ConversacionRequest request);
    ComprobantePagoResponse crearComprobante(ComprobantePagoRequest request);
    void delete(Long id);
}
