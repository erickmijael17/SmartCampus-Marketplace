package com.upeu.media.service.impl;

import com.upeu.media.dto.MediaFileRequest;
import com.upeu.media.dto.MediaFileResponse;
import com.upeu.media.entity.MediaFile;
import com.upeu.media.repository.MediaFileRepository;
import com.upeu.media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MediaFileServiceImpl implements MediaFileService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final MediaFileRepository repository;
    private final Path uploadRoot;
    private final String publicBaseUrl;
    private final String publicPath;

    @Autowired
    public MediaFileServiceImpl(
            MediaFileRepository repository,
            @Value("${media.storage.upload-dir:uploads/media}") String uploadDir,
            @Value("${media.public-base-url:http://localhost:18080}") String publicBaseUrl,
            @Value("${media.public-path:/api/v1/media/files}") String publicPath
    ) {
        this(repository, Path.of(uploadDir), publicBaseUrl, publicPath);
    }

    private MediaFileServiceImpl(MediaFileRepository repository, Path uploadRoot, String publicBaseUrl, String publicPath) {
        this.repository = repository;
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.publicPath = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
    }

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
    public MediaFileResponse upload(MultipartFile file, Long idUploader, Long idPublicacion) {
        validateImage(file);

        try {
            Files.createDirectories(uploadRoot);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "imagen" : file.getOriginalFilename());
            String extension = extensionOf(originalName);
            String storedName = UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(storedName).normalize();

            if (!target.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Ruta de archivo invalida");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            MediaFile entity = new MediaFile();
            entity.setNombreOriginal(originalName);
            entity.setNombreAlmacen(storedName);
            entity.setRuta(target.toString());
            entity.setUrl(publicPath + "/" + storedName);
            entity.setTipoMime(file.getContentType());
            entity.setTamanoBytes(file.getSize());
            entity.setIdUploader(idUploader);
            entity.setIdPublicacion(idPublicacion);
            entity.setEntidadTipo("PUBLICACION");

            return mapToResponse(repository.save(entity));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadFile(String storedName) {
        try {
            Path file = uploadRoot.resolve(storedName).normalize();
            if (!file.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Ruta de archivo invalida");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Archivo no encontrado");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Ruta de archivo invalida", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String contentTypeOf(String storedName) {
        String storedMime = repository.findByNombreAlmacen(storedName)
                .map(MediaFile::getTipoMime)
                .filter(mime -> mime != null && !mime.isBlank())
                .orElse(null);
        if (storedMime != null) {
            return storedMime;
        }

        try {
            Path file = uploadRoot.resolve(storedName).normalize();
            if (file.startsWith(uploadRoot)) {
                String detectedMime = Files.probeContentType(file);
                if (detectedMime != null && !detectedMime.isBlank()) {
                    return detectedMime;
                }
            }
        } catch (IOException ignored) {
            // Fall through to a safe binary type when the OS cannot infer the MIME type.
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
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
        String url = request.getUrl();
        entity.setUrl(url);
        entity.setNombreOriginal("remote-image");
        entity.setNombreAlmacen("remote-" + UUID.randomUUID());
        entity.setRuta(url);
        entity.setTipoMime(request.getTipoMime() == null ? "image/remote" : request.getTipoMime());
        entity.setTamanoBytes(request.getTamanoBytes() == null ? 0L : request.getTamanoBytes());
        entity.setIdUploader(request.getIdUploader());
        entity.setIdPublicacion(request.getIdPublicacion());
        entity.setEntidadTipo("PUBLICACION");
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

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen es obligatoria");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Formato de imagen no permitido");
        }
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return ".jpg";
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
