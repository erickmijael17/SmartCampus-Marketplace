package com.upeu.favoritos.controller;

import com.upeu.favoritos.dto.FavoritoRequest;
import com.upeu.favoritos.dto.FavoritoResponse;
import com.upeu.favoritos.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService service;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - Favorito Service is up and running via Gateway!");
    }

    @GetMapping
    public ResponseEntity<List<FavoritoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FavoritoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<FavoritoResponse> create(@RequestBody FavoritoRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FavoritoResponse> update(@PathVariable Long id, @RequestBody FavoritoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
