package com.upeu.pagos.controller;

import com.upeu.pagos.dto.PagoRequest;
import com.upeu.pagos.dto.PagoResponse;
import com.upeu.pagos.service.PagoService;
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
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagosService;

    @PostMapping
    public ResponseEntity<PagoResponse> create(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagosService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> findAll() {
        return ResponseEntity.ok(pagosService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pagosService.findById(id));
    }

    @GetMapping("/usuario/{idComprador}")
    public ResponseEntity<List<PagoResponse>> findByComprador(@PathVariable Long idComprador) {
        return ResponseEntity.ok(pagosService.findByComprador(idComprador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponse> update(@PathVariable Long id, @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagosService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagosService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

