package com.upeu.media.service;

import com.upeu.media.dto.MediaFileRequest;
import com.upeu.media.dto.MediaFileResponse;
import java.util.List;

public interface MediaFileService {
    List<MediaFileResponse> findAll();
    MediaFileResponse findById(Long id);
    MediaFileResponse create(MediaFileRequest request);
    MediaFileResponse update(Long id, MediaFileRequest request);
    void delete(Long id);
}
