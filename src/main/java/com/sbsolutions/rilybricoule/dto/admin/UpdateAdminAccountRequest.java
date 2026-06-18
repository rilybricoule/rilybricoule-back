package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Data;

@Data
public class UpdateAdminAccountRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String roleName;
    private Boolean enabled;
}