package com.upeu.carrito.controller;

import com.upeu.carrito.dto.CarritoRequest;
import com.upeu.carrito.dto.CarritoResponse;
import com.upeu.carrito.service.CarritoService;
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
@RequestMapping("/api/v1/carritos")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping
    public ResponseEntity<CarritoResponse> create(@Valid @RequestBody CarritoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CarritoResponse>> findAll() {
        return ResponseEntity.ok(carritoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.findById(id));
    }

    @GetMapping("/usuario/{idComprador}")
    public ResponseEntity<List<CarritoResponse>> findByComprador(@PathVariable Long idComprador) {
        return ResponseEntity.ok(carritoService.findByComprador(idComprador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoResponse> update(@PathVariable Long id, @Valid @RequestBody CarritoRequest request) {
        return ResponseEntity.ok(carritoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carritoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
