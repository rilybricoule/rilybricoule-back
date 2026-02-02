package com.sbsolutions.rilybricoule.auth.service;

import com.sbsolutions.rilybricoule.auth.entity.RefreshToken;
import com.sbsolutions.rilybricoule.auth.repository.RefreshTokenRepository;
import com.sbsolutions.rilybricoule.user.entity.User;
import com.sbsolutions.rilybricoule.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(String username) {

        User user = userRepository.findByUsername(username).orElseThrow();
        repository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        return repository.save(token);
    }

    public User validateAndRotate(String token,
                                  HttpServletResponse response) {

        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        repository.delete(refreshToken);

        return user;
    }

    public String extractFromCookie(HttpServletRequest request) {

        if (request.getCookies() == null)
            throw new RuntimeException("No cookies");

        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .orElseThrow()
                .getValue();
    }
}
