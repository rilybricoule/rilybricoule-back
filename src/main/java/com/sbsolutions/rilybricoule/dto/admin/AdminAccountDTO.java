package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAccountDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String photoUrl;
    private boolean enabled;
    private boolean twoFAEnabled;
    private String roleName;
    private List<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}