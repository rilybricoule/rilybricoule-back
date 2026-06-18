package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Data;

@Data
public class CreateAdminAccountRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String roleName;
}