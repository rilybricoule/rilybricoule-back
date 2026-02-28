package com.sbsolutions.rilybricoule.security.domain.port.in;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.dto.SocialLoginRequest;

public interface AuthUseCase {

    JwtResponse register(RegisterRequest request, String ipAddress, String userAgent);

    JwtResponse login(LoginRequest request, String ipAddress, String userAgent);

    JwtResponse socialLogin(SocialLoginRequest request, String ipAddress, String userAgent);

    JwtResponse refreshToken(String refreshToken, String ipAddress, String userAgent);

    boolean validateToken(String accessToken);

    void logout(String refreshToken, String ipAddress, String userAgent);
}
