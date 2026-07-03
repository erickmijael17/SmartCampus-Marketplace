package com.upeu.chat.service;

import com.upeu.chat.dto.MensajeRequest;
import com.upeu.chat.dto.MensajeResponse;
import java.util.List;

public interface MensajeService {

    List<MensajeResponse> findByConversacionId(Long conversacionId);

    MensajeResponse create(Long conversacionId, MensajeRequest request);
}
