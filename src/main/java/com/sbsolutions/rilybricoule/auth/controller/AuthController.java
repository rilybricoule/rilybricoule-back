package com.sbsolutions.rilybricoule.auth.controller;

import com.sbsolutions.rilybricoule.auth.dto.JwtResponse;
import com.sbsolutions.rilybricoule.auth.dto.LoginRequest;
import com.sbsolutions.rilybricoule.auth.dto.RefreshTokenRequest;
import com.sbsolutions.rilybricoule.auth.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request,
                             HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(HttpServletRequest request,
                               HttpServletResponse response) {
        System.out.println(Arrays.toString(request.getCookies()));

        return authService.refresh(request, response);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {
        authService.logout(request, response);
    }





}
