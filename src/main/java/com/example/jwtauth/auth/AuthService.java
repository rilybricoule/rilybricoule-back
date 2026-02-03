package com.example.jwtauth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.example.jwtauth.auth.dto.AuthResponse;
import com.example.jwtauth.auth.dto.LoginRequest;
import com.example.jwtauth.auth.dto.RegisterRequest;
import com.example.jwtauth.security.JwtUtil;
import com.example.jwtauth.user.User;
import com.example.jwtauth.user.UserService;

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