package com.sbsolutions.rilybricoule.auth.dto;

public record JwtResponse(
        String accessToken,
//        String refreshToken,
        String tokenType,
        String username
) {}
