package com.sbsolutions.rilybricoule.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}
