package com.sbsolutions.rilybricoule.security.application;

import com.sbsolutions.rilybricoule.dto.*;
import com.sbsolutions.rilybricoule.dto.admin.Login2FARequiredResponse;
import com.sbsolutions.rilybricoule.dto.admin.TwoFASetupResponse;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.exceptions.EmailAlreadyExistsException;
import com.sbsolutions.rilybricoule.exceptions.RefreshTokenExpiredException;
import com.sbsolutions.rilybricoule.exceptions.RefreshTokenNotFoundException;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.security.domain.model.RefreshToken;
import com.sbsolutions.rilybricoule.security.domain.port.in.AuthUseCase;
import com.sbsolutions.rilybricoule.security.domain.port.out.AuditLogPort;
import com.sbsolutions.rilybricoule.security.domain.port.out.RefreshTokenRepositoryPort;
import com.sbsolutions.rilybricoule.security.domain.port.out.TokenProviderPort;
import com.sbsolutions.rilybricoule.security.infrastructure.oauth2.OAuth2TokenVerifier;
import com.sbsolutions.rilybricoule.security.infrastructure.oauth2.OAuth2TokenVerifierFactory;
import com.sbsolutions.rilybricoule.services.OtpService;
import com.sbsolutions.rilybricoule.services.TwoFAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuditLogPort auditLog;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final OAuth2TokenVerifierFactory oAuth2TokenVerifierFactory;
    private final OtpService otpService;
    private final TwoFAService twoFAService;

    @Override
    @Transactional
    public void register(RegisterRequest request, String ipAddress, String userAgent) {
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
        user.setEnabled(false);

        RoleName roleName = "PRESTATAIRE".equalsIgnoreCase(request.getRole())
                ? RoleName.ROLE_PRESTATAIRE
                : RoleName.ROLE_CLIENT;

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        userRepository.save(user);

        otpService.generateAndSendOtp(user.getEmail(), OtpPurpose.EMAIL_VERIFICATION);
    }

    @Override
    @Transactional
    public JwtResponse verifyEmailAndActivate(String email, String code, String ipAddress, String userAgent) {
        boolean valid = otpService.verifyOtp(email, code, OtpPurpose.EMAIL_VERIFICATION);
        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logRegister(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public JwtResponse changePasswordRequired(
            String email,
            String currentPassword,
            String newPassword,
            String confirmPassword,
            String ipAddress,
            String userAgent
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isMustChangePassword()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password change is not required"
            );
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            auditLog.logLoginFailure(email, ipAddress, userAgent, "Invalid current password");
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logLoginSuccess(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }


    @Override
    @Transactional
    public Object login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException ex) {
            auditLog.logLoginFailure(
                    request.getEmail(),
                    ipAddress,
                    userAgent,
                    "Account disabled by Super Admin"
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Votre compte a été désactivé par le Super Admin."
            );
        } catch (AuthenticationException ex) {
            auditLog.logLoginFailure(request.getEmail(), ipAddress, userAgent, ex.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.isMustChangePassword()) {
            return Map.of(
                    "requiresPasswordChange", true,
                    "email", user.getEmail()
            );
        }


        if (user.isTwoFAEnabled()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String tempToken = tokenProvider.generateTemp2FAToken(userDetails);

            return Login2FARequiredResponse.builder()
                    .requires2FA(true)
                    .tempToken(tempToken)
                    .build();
        }
        refreshTokenRepository.revokeAllByUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logLoginSuccess(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            throw new BadCredentialsException("Unauthorized");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }


    @Override
    @Transactional
    public JwtResponse verify2FA(String tempTokenHeader, String code) {
        String token = tempTokenHeader.replace("Bearer ", "");

        String tokenType = tokenProvider.extractTokenType(token);

        if (!"TEMP_2FA".equals(tokenType)) {
            throw new BadCredentialsException("Invalid 2FA token");
        }

        String email = tokenProvider.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid 2FA token"));

        if (user.getTwoFASecret() == null || user.getTwoFASecret().isBlank()) {
            throw new IllegalArgumentException("2FA is not configured");
        }

        boolean valid = twoFAService.verifyCode(user.getTwoFASecret(), code);

        if (!valid) {
            throw new BadCredentialsException("Invalid 2FA code");
        }

        refreshTokenRepository.revokeAllByUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logLoginSuccess(user.getEmail(), null, null);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }


    @Override
    @Transactional
    public JwtResponse socialLogin(SocialLoginRequest request, String ipAddress, String userAgent) {
        AuthProvider provider = AuthProvider.valueOf(request.getProvider().toUpperCase());
        OAuth2TokenVerifier verifier = oAuth2TokenVerifierFactory.getVerifier(provider);
        SocialUserInfo socialUser = verifier.verify(request.getIdToken());

        Optional<User> existingUser = userRepository.findByEmail(socialUser.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(provider);
                user.setProviderId(socialUser.getProviderId());
                if (socialUser.getPhotoUrl() != null) {
                    user.setPhotoUrl(socialUser.getPhotoUrl());
                }
                user = userRepository.save(user);
            }
        } else {
            Client client = new Client();
            client.setEmail(socialUser.getEmail());
            client.setFirstName(socialUser.getFirstName() != null ? socialUser.getFirstName() : "");
            client.setLastName(socialUser.getLastName() != null ? socialUser.getLastName() : "");
            client.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            client.setAuthProvider(provider);
            client.setProviderId(socialUser.getProviderId());
            client.setPhotoUrl(socialUser.getPhotoUrl());
            client.setEnabled(true);

            Role role = roleRepository.findByRoleName(RoleName.ROLE_CLIENT)
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_CLIENT"));
            client.setRoles(Set.of(role));

            user = userRepository.save(client);
            auditLog.logRegister(user.getEmail(), ipAddress, userAgent);
        }

        refreshTokenRepository.revokeAllByUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logLoginSuccess(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(String refreshTokenStr, String ipAddress, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.revokeToken(refreshToken);
            auditLog.logTokenRefreshFailure(ipAddress, userAgent, "Refresh token expired");
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.revokeToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken newRefreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logTokenRefresh(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Override
    public boolean validateToken(String accessToken) {
        try {
            String userEmail = tokenProvider.extractEmail(accessToken);
            if (userEmail == null) {
                return false;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            return tokenProvider.isTokenValid(accessToken, userDetails);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr, String ipAddress, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        User user = refreshToken.getUser();
        refreshTokenRepository.revokeAllByUser(user);

        auditLog.logLogout(user.getEmail(), ipAddress, userAgent);
    }
    @Override
    @Transactional
    public TwoFASetupResponse setup2FA() {
        User user = getCurrentUser();

        String secret = user.getTwoFASecret();

        if (secret == null || secret.isBlank()) {
            secret = twoFAService.generateSecret();
            user.setTwoFASecret(secret);
            userRepository.save(user);
        }

        String qrBase64 = twoFAService.generateQrCodeBase64(secret, user.getEmail());

        return TwoFASetupResponse.builder()
                .email(user.getEmail())
                .secret(secret)
                .qrCodeBase64(qrBase64)
                .enabled(user.isTwoFAEnabled())
                .build();
    }
    @Override
    @Transactional
    public void enable2FA(String code) {
        User user = getCurrentUser();

        if (user.getTwoFASecret() == null || user.getTwoFASecret().isBlank()) {
            throw new IllegalArgumentException("2FA setup is required before enabling");
        }

        boolean valid = twoFAService.verifyCode(user.getTwoFASecret(), code);

        if (!valid) {
            throw new IllegalArgumentException("Invalid 2FA code");
        }

        user.setTwoFAEnabled(true);
        userRepository.save(user);
    }


    @Override
    @Transactional
    public void disable2FA(String code) {
        User user = getCurrentUser();

        if (!user.isTwoFAEnabled()) {
            return;
        }

        boolean valid = twoFAService.verifyCode(user.getTwoFASecret(), code);

        if (!valid) {
            throw new IllegalArgumentException("Invalid 2FA code");
        }

        user.setTwoFAEnabled(false);
        userRepository.save(user);
    }
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }
        otpService.generateAndSendOtp(email, OtpPurpose.PASSWORD_RESET);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        boolean valid = otpService.verifyOtp(email, code, OtpPurpose.PASSWORD_RESET);
        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);
    }

    @Override
    @Transactional
    public void resendOtp(String email, String purpose) {
        OtpPurpose otpPurpose = OtpPurpose.valueOf(purpose.toUpperCase());

        if (otpPurpose == OtpPurpose.EMAIL_VERIFICATION) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || user.isEnabled()) {
                throw new IllegalArgumentException("Invalid request");
            }
        } else {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return;
            }
        }

        otpService.generateAndSendOtp(email, otpPurpose);
    }


    private JwtResponse buildJwtResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .sorted()
                .collect(Collectors.toList());

        Role primaryRole = user.getRoles().stream()
                .filter(role -> role.getRoleName() == RoleName.ROLE_SUPER_ADMIN)
                .findFirst()
                .orElseGet(() -> user.getRoles().stream()
                        .filter(role -> role.getRoleName() == RoleName.ROLE_MODERATEUR)
                        .findFirst()
                        .orElseGet(() -> user.getRoles().stream()
                                .filter(role -> role.getRoleName() == RoleName.ROLE_SUPPORT)
                                .findFirst()
                                .orElse(user.getRoles().iterator().next())));

        List<String> permissions = primaryRole.getPermissions().stream()
                .map(permission -> permission.getPermissionName().name())
                .sorted()
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .roleName(primaryRole.getRoleName().name())
                .permissions(permissions)
                .build();
    }
}
