package com.sbsolutions.rilybricoule.security.adapter.in.web;

import com.sbsolutions.rilybricoule.dto.*;
import com.sbsolutions.rilybricoule.security.domain.port.in.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request,
                                                         HttpServletRequest httpRequest) {
        authUseCase.register(request, extractIp(httpRequest), extractUserAgent(httpRequest));
        return ResponseEntity.ok(Map.of("message", "Registration successful. Please check your email for the verification code."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<JwtResponse> verifyEmail(@Valid @RequestBody VerifyOtpRequest request,
                                                    HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authUseCase.verifyEmailAndActivate(
                request.getEmail(), request.getCode(), extractIp(httpRequest), extractUserAgent(httpRequest)));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authUseCase.login(request, extractIp(httpRequest), extractUserAgent(httpRequest)));
    }

    @PostMapping("/social-login")
    public ResponseEntity<JwtResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request,
                                                    HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authUseCase.socialLogin(request, extractIp(httpRequest), extractUserAgent(httpRequest)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authUseCase.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset code has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authUseCase.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestParam String email,
                                                          @RequestParam String purpose) {
        authUseCase.resendOtp(email, purpose);
        return ResponseEntity.ok(Map.of("message", "OTP code has been resent."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authUseCase.refreshToken(request.getRefreshToken(), extractIp(httpRequest), extractUserAgent(httpRequest)));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(Map.of("valid", authUseCase.validateToken(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request,
                                                       HttpServletRequest httpRequest) {
        authUseCase.logout(request.getRefreshToken(), extractIp(httpRequest), extractUserAgent(httpRequest));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
