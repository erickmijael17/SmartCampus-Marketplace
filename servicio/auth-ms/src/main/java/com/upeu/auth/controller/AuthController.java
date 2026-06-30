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
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String accessToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.getUserInfo(accessToken));
    }

    @GetMapping("/users")
    public ResponseEntity<List<PersonaDto.Response>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        authService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
