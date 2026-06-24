package com.upeu.chat.service.impl;

import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.entity.Conversacion;
import com.upeu.chat.repository.ConversacionRepository;
import com.upeu.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversacionRepository conversacionRepository;

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
}
