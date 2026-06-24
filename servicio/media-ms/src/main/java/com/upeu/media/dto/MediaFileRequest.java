package com.upeu.media.dto;

import lombok.Data;

@Data
public class MediaFileRequest {
    private String url;
    private String tipoMime;
    private Long tamanoBytes;
    private Long idUploader;
    private Long idPublicacion;
}
