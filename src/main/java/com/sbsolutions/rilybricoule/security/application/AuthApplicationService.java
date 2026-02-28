package com.sbsolutions.rilybricoule.security.application;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.dto.SocialLoginRequest;
import com.sbsolutions.rilybricoule.dto.SocialUserInfo;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest request, String ipAddress, String userAgent) {
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
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logRegister(user.getEmail(), ipAddress, userAgent);

        return buildJwtResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            auditLog.logLoginFailure(request.getEmail(), ipAddress, userAgent, ex.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        refreshTokenRepository.revokeAllByUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.createRefreshToken(user);

        auditLog.logLoginSuccess(user.getEmail(), ipAddress, userAgent);

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

    private JwtResponse buildJwtResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
