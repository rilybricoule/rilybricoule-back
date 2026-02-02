package com.sbsolutions.rilybricoule.auth.service;

import com.sbsolutions.rilybricoule.auth.dto.JwtResponse;
import com.sbsolutions.rilybricoule.auth.dto.LoginRequest;
import com.sbsolutions.rilybricoule.auth.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.auth.entity.RefreshToken;
import com.sbsolutions.rilybricoule.auth.repository.RefreshTokenRepository;
import com.sbsolutions.rilybricoule.security.CustomUserDetailsService;
import com.sbsolutions.rilybricoule.security.JwtService;
import com.sbsolutions.rilybricoule.user.entity.Role;
import com.sbsolutions.rilybricoule.user.entity.User;
import com.sbsolutions.rilybricoule.user.repository.RoleRepository;
import com.sbsolutions.rilybricoule.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public JwtResponse login(LoginRequest request,
                             HttpServletResponse response) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());

        String accessToken =
                jwtService.generateAccessToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(userDetails.getUsername());

        setRefreshTokenCookie(response, refreshToken.getToken());

        return new JwtResponse(
                accessToken,
                "Bearer",
                userDetails.getUsername()
        );
    }

    public JwtResponse refresh(HttpServletRequest request,
                               HttpServletResponse response) {

        String token =
                refreshTokenService.extractFromCookie(request);

        User user =
                refreshTokenService.validateAndRotate(token, response);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken =
                jwtService.generateAccessToken(userDetails);

        return new JwtResponse(
                accessToken,
                "Bearer",
                user.getUsername()
        );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // true in prod
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {

        String refreshToken = extractRefreshToken(request);

        // Delete refresh token from DB
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);

        // Clear cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true in prod
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null)
            return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public void register(RegisterRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(roleUser));

        userRepository.save(user);
    }
}

