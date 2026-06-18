package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwoFASetupResponse {
    private String email;
    private String secret;
    private String qrCodeBase64;
    private boolean enabled;
}