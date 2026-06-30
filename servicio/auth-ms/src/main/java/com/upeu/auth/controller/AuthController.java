package com.upeu.auth.controller;

import com.upeu.auth.dto.AuthLoginRequest;
import com.upeu.auth.dto.AuthLoginResponse;
import com.upeu.auth.dto.AuthRegisterRequest;
import com.upeu.auth.dto.PersonaDto;
import com.upeu.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthLoginResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        return ResponseEntity.ok(authService.me(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<PersonaDto.Response> getProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String accessToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.getProfile(accessToken));
    }

    @PutMapping("/profile")
    public ResponseEntity<PersonaDto.Response> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody PersonaDto.Request body) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String accessToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.updateProfile(accessToken, body));
    }
}
