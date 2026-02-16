package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.exceptions.EmailAlreadyExistsException;
import com.sbsolutions.rilybricoule.exceptions.InvalidTokenException;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user;

        if ("PRESTATAIRE".equalsIgnoreCase(request.getRole())) {
            Prestataire prestataire = new Prestataire();
            prestataire.setBusinessName(request.getBusinessName());
            prestataire.setCin(request.getCin());
            prestataire.setDescription(request.getDescription());
            prestataire.setVerified(false);
            prestataire.setAvailable(true);
            user = prestataire;
        } else {
            user = new Client();
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEnabled(true);

        RoleName roleName = "PRESTATAIRE".equalsIgnoreCase(request.getRole())
                ? RoleName.ROLE_PRESTATAIRE
                : RoleName.ROLE_CLIENT;

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);

        log.info("User registered successfully: {}", user.getEmail());
        return buildJwtResponse(user, accessToken);
    }

    public JwtResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());
        return buildJwtResponse(user, accessToken);
    }

    public String refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractEmail(token);

        if (userEmail == null) {
            throw new InvalidTokenException("Could not extract user from token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Token is invalid or expired");
        }

        log.info("Token refreshed for user: {}", userEmail);
        return jwtService.generateToken(userDetails);
    }

    public boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authHeader.substring(7);
            String userEmail = jwtService.extractEmail(token);

            if (userEmail == null) {
                return false;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception ex) {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private JwtResponse buildJwtResponse(User user, String accessToken) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
