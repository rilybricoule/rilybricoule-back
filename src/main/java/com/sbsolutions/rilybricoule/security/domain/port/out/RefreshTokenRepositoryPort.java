package com.sbsolutions.rilybricoule.security.domain.port.out;

import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.security.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllByUser(User user);

    void revokeToken(RefreshToken refreshToken);
}
