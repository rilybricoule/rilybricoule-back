package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequiredRequest {
    private String email;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}