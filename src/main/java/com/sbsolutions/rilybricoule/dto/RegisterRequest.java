package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;

    // "CLIENT" or "PRESTATAIRE"
    private String role;

    // Prestataire-specific fields (optional, only used when role = PRESTATAIRE)
    private String businessName;
    private String cin;
    private String description;
}
