package com.upeu.producto.controller;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - Producto Service is up and running via Gateway!");
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> create(@Valid @RequestBody ProductoRequest request,
                                                   HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        ProductoResponse response = productoService.create(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> findAll() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ProductoRequest request,
                                                   HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        return ResponseEntity.ok(productoService.update(id, request, token));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        productoService.delete(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<?> findDetalleById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findDetalleById(id));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
