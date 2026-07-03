package com.upeu.media.service;

import com.upeu.media.dto.MediaFileRequest;
import com.upeu.media.dto.MediaFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaFileService {
    List<MediaFileResponse> findAll();
    MediaFileResponse findById(Long id);
    MediaFileResponse create(MediaFileRequest request);
    MediaFileResponse upload(MultipartFile file, Long idUploader, Long idPublicacion);
    Resource loadFile(String storedName);
    String contentTypeOf(String storedName);
    MediaFileResponse update(Long id, MediaFileRequest request);
    void delete(Long id);
}
