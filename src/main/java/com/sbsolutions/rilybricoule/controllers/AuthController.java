package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String newToken = authService.refreshToken(authHeader);
        return ResponseEntity.ok(Map.of("accessToken", newToken));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        boolean valid = authService.validateToken(authHeader);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}