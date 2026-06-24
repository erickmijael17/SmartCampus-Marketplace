package com.upeu.media.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MediaFileResponse {
    private Long id;
    private String url;
    private String tipoMime;
    private Long tamanoBytes;
    private Long idUploader;
    private Long idPublicacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
