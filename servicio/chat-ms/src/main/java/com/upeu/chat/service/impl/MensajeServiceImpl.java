package com.upeu.chat.service.impl;

import com.upeu.chat.dto.MensajeRequest;
import com.upeu.chat.dto.MensajeResponse;
import com.upeu.chat.entity.Mensaje;
import com.upeu.chat.repository.ConversacionRepository;
import com.upeu.chat.repository.MensajeRepository;
import com.upeu.chat.service.MensajeService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MensajeResponse> findByConversacionId(Long conversacionId) {
        ensureConversacionExists(conversacionId);
        return mensajeRepository.findByIdConversacionOrderByCreadoEnAsc(conversacionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MensajeResponse create(Long conversacionId, MensajeRequest request) {
        ensureConversacionExists(conversacionId);

        Mensaje entity = Mensaje.builder()
                .idConversacion(conversacionId)
                .idRemitente(request.getIdRemitente())
                .contenido(request.getContenido())
                .tipoRemitente("USUARIO")
                .tipoMensaje("TEXTO")
                .leido(request.getLeido() != null ? request.getLeido() : Boolean.FALSE)
                .build();

        return mapToResponse(mensajeRepository.save(entity));
    }

    private void ensureConversacionExists(Long conversacionId) {
        conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new RuntimeException("Conversacion no encontrada con ID: " + conversacionId));
    }

    private MensajeResponse mapToResponse(Mensaje entity) {
        MensajeResponse response = new MensajeResponse();
        response.setId(entity.getId());
        response.setIdConversacion(entity.getIdConversacion());
        response.setIdRemitente(entity.getIdRemitente());
        response.setContenido(entity.getContenido());
        response.setTipoRemitente(entity.getTipoRemitente());
        response.setTipoMensaje(entity.getTipoMensaje());
        response.setIdOrden(entity.getIdOrden());
        response.setPagoId(entity.getPagoId());
        response.setMpPaymentId(entity.getMpPaymentId());
        response.setLeido(entity.getLeido());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
