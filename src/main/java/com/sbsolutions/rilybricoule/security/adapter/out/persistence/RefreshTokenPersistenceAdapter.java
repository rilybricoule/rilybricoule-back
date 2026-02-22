package com.sbsolutions.rilybricoule.security.adapter.out.persistence;

import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.security.domain.model.RefreshToken;
import com.sbsolutions.rilybricoule.security.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();
        return refreshTokenJpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByTokenAndRevokedFalse(token);
    }

    @Override
    @Transactional
    public void revokeAllByUser(User user) {
        refreshTokenJpaRepository.revokeAllByUserId(user.getId());
    }

    @Override
    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenJpaRepository.save(refreshToken);
    }
}
