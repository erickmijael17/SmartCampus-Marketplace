package com.upeu.search.controller;

import com.upeu.search.dto.BusquedaHistorialRequest;
import com.upeu.search.dto.BusquedaHistorialResponse;
import com.upeu.search.service.BusquedaHistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class BusquedaHistorialController {

    private final BusquedaHistorialService service;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - BusquedaHistorial Service is up and running via Gateway!");
    }

    @GetMapping
    public ResponseEntity<List<BusquedaHistorialResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusquedaHistorialResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<BusquedaHistorialResponse> create(@RequestBody BusquedaHistorialRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusquedaHistorialResponse> update(@PathVariable Long id, @RequestBody BusquedaHistorialRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
