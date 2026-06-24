package com.upeu.media.service.impl;

import com.upeu.media.dto.MediaFileRequest;
import com.upeu.media.dto.MediaFileResponse;
import com.upeu.media.entity.MediaFile;
import com.upeu.media.repository.MediaFileRepository;
import com.upeu.media.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final MediaFileRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<MediaFileResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MediaFileResponse findById(Long id) {
        MediaFile entity = repository.findById(id).orElseThrow(() -> new RuntimeException("MediaFile no encontrado"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public MediaFileResponse create(MediaFileRequest request) {
        MediaFile entity = new MediaFile();
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public MediaFileResponse update(Long id, MediaFileRequest request) {
        MediaFile entity = repository.findById(id).orElseThrow(() -> new RuntimeException("MediaFile no encontrado"));
        mapToEntity(request, entity);
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapToEntity(MediaFileRequest request, MediaFile entity) {
        entity.setUrl(request.getUrl());
        entity.setTipoMime(request.getTipoMime());
        entity.setTamanoBytes(request.getTamanoBytes());
        entity.setIdUploader(request.getIdUploader());
        entity.setIdPublicacion(request.getIdPublicacion());
    }

    private MediaFileResponse mapToResponse(MediaFile entity) {
        MediaFileResponse response = new MediaFileResponse();
        response.setId(entity.getId());
        response.setUrl(entity.getUrl());
        response.setTipoMime(entity.getTipoMime());
        response.setTamanoBytes(entity.getTamanoBytes());
        response.setIdUploader(entity.getIdUploader());
        response.setIdPublicacion(entity.getIdPublicacion());
        response.setCreadoEn(entity.getCreadoEn());
        response.setActualizadoEn(entity.getActualizadoEn());
        return response;
    }
}
