package com.sbsolutions.rilybricoule.security.domain.port.out;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenProviderPort {

    String generateAccessToken(UserDetails userDetails);

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
