package com.upeu.ordenes.controller;

import com.upeu.ordenes.dto.OrdenRequest;
import com.upeu.ordenes.dto.OrdenResponse;
import com.upeu.ordenes.service.OrdenService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenesService;

    @PostMapping
    public ResponseEntity<OrdenResponse> create(@Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenesService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> findAll() {
        return ResponseEntity.ok(ordenesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenesService.findById(id));
    }

    @GetMapping("/usuario/{idComprador}")
    public ResponseEntity<List<OrdenResponse>> findByComprador(@PathVariable Long idComprador) {
        return ResponseEntity.ok(ordenesService.findByComprador(idComprador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenResponse> update(@PathVariable Long id, @Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.ok(ordenesService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordenesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

