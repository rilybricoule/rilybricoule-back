package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Login2FARequiredResponse {
    private boolean requires2FA;
    private String tempToken;
}