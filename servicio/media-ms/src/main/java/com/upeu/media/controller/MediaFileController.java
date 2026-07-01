package com.upeu.media.controller;

import com.upeu.media.dto.MediaFileRequest;
import com.upeu.media.dto.MediaFileResponse;
import com.upeu.media.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileService service;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - MediaFile Service is up and running via Gateway!");
    }

    @GetMapping
    public ResponseEntity<List<MediaFileResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<MediaFileResponse> create(@RequestBody MediaFileRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFileResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long idUploader,
            @RequestParam Long idPublicacion
    ) {
        return new ResponseEntity<>(service.upload(file, idUploader, idPublicacion), HttpStatus.CREATED);
    }

    @GetMapping("/files/{storedName:.+}")
    public ResponseEntity<Resource> findFile(@PathVariable String storedName) {
        Resource resource = service.loadFile(storedName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(service.contentTypeOf(storedName)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MediaFileResponse> update(@PathVariable Long id, @RequestBody MediaFileRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
