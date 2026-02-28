package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotBlank(message = "ID token is required")
    private String idToken;
}
