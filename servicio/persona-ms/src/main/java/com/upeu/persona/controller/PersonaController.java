package com.upeu.persona.controller;

import com.upeu.persona.dto.PersonaDto;
import com.upeu.persona.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping
    public ResponseEntity<List<PersonaDto.Response>> findAll() {
        return ResponseEntity.ok(personaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<PersonaDto.Response> findMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // 'sub' claim de Keycloak (UUID)
        return ResponseEntity.ok(personaService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<PersonaDto.Response> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PersonaDto.Request request) {
        String userId = jwt.getSubject(); // 'sub' claim de Keycloak (UUID)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personaService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PersonaDto.Request request) {
        return ResponseEntity.ok(personaService.update(id, request));
    }
}
