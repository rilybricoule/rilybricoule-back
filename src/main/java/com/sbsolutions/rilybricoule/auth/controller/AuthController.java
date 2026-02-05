package com.sbsolutions.rilybricoule.auth.controller;

import com.sbsolutions.rilybricoule.auth.dto.AuthResponse;
import com.sbsolutions.rilybricoule.auth.dto.LoginRequest;
import com.sbsolutions.rilybricoule.auth.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
