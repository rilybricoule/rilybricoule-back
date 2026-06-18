package com.sbsolutions.rilybricoule.dto.admin;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFAVerifyRequest {
    @NotBlank(message = "2FA code is required")
    private String code;
}