package com.sbsolutions.rilybricoule.security.domain.port.in;

import com.sbsolutions.rilybricoule.dto.*;
import com.sbsolutions.rilybricoule.dto.admin.TwoFASetupResponse;

public interface AuthUseCase {

    void register(RegisterRequest request, String ipAddress, String userAgent);

    JwtResponse verifyEmailAndActivate(String email, String code, String ipAddress, String userAgent);

    Object login(LoginRequest request, String ipAddress, String userAgent);

    JwtResponse socialLogin(SocialLoginRequest request, String ipAddress, String userAgent);

    JwtResponse refreshToken(String refreshToken, String ipAddress, String userAgent);

    boolean validateToken(String accessToken);

    void logout(String refreshToken, String ipAddress, String userAgent);

    void forgotPassword(String email);

    void resetPassword(String email, String code, String newPassword);

    void resendOtp(String email, String purpose);

    JwtResponse verify2FA(String tempTokenHeader, String code);

    TwoFASetupResponse setup2FA();

    void enable2FA(String code);

    void disable2FA(String code);

    JwtResponse changePasswordRequired(
            String email,
            String currentPassword,
            String newPassword,
            String confirmPassword,
            String ipAddress,
            String userAgent
    );
}
