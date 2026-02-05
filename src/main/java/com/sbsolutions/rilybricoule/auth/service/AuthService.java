package com.sbsolutions.rilybricoule.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.sbsolutions.rilybricoule.auth.dto.AuthResponse;
import com.sbsolutions.rilybricoule.auth.dto.LoginRequest;
import com.sbsolutions.rilybricoule.auth.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.auth.security.JwtUtil;
import com.sbsolutions.rilybricoule.auth.user.User;
import com.sbsolutions.rilybricoule.auth.user.UserService;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Authentication successful → fetch domain user
        User user = userService.getUserByEmail(request.getEmail());

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole()
        );

        return new AuthResponse(token);

    } catch (AuthenticationException e) {
        throw new RuntimeException("Invalid credentials");
    }
    }
}

