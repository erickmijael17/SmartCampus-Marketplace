package com.upeu.media.service.impl;

import com.upeu.media.dto.MediaFileResponse;
import com.upeu.media.entity.MediaFile;
import com.upeu.media.repository.MediaFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaFileServiceImplTest {

    @TempDir
    Path uploadDir;

    @Test
    void storesUploadedImageAndReturnsPublicGatewayUrl() throws Exception {
        MediaFileRepository repository = mock(MediaFileRepository.class);
        when(repository.save(any(MediaFile.class))).thenAnswer(invocation -> {
            MediaFile entity = invocation.getArgument(0);
            entity.setId(15L);
            return entity;
        });
        MediaFileServiceImpl service = new MediaFileServiceImpl(
                repository,
                uploadDir,
                "http://localhost:18080",
                "/api/v1/media/files"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto producto.JPG",
                "image/jpeg",
                "fake-image".getBytes()
        );

        MediaFileResponse response = service.upload(file, 9L, 20L);

        assertThat(response.getUrl()).startsWith("http://localhost:18080/api/v1/media/files/");
        assertThat(response.getTipoMime()).isEqualTo("image/jpeg");
        assertThat(response.getTamanoBytes()).isEqualTo(10L);
        assertThat(response.getIdUploader()).isEqualTo(9L);
        assertThat(response.getIdPublicacion()).isEqualTo(20L);
        assertThat(Files.list(uploadDir)).hasSize(1);
    }
}
