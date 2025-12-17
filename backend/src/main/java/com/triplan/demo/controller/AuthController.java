package com.triplan.demo.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.triplan.demo.dto.AuthResponse;
import com.triplan.demo.dto.LoginRequest;
import com.triplan.demo.dto.RegisterRequest;
import com.triplan.demo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // Витягуємо email з токена (можна через SecurityContext)
        String token = authHeader.substring(7);
        // Тут можна додати логіку витягування email з токена
        // Для простоти використовуємо SecurityContext
        return ResponseEntity.ok().build();
    }
}
