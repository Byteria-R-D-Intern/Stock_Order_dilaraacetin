package com.example.stock_order.adapters.web;


import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.auth.AuthResponse;
import com.example.stock_order.adapters.web.dto.auth.LoginRequest;
import com.example.stock_order.adapters.web.dto.auth.RegisterRequest;
import com.example.stock_order.application.AuthService;
import com.example.stock_order.config.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody @Valid RegisterRequest req) {
        Long id = authService.register(req.email(), req.password());
        return ResponseEntity.ok(id);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        String token = authService.login(req.email(), req.password());
        String role = jwtService.extractRole(token);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", role));
    }
}