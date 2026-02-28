package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {

    private String email;
    private String firstName;
    private String lastName;
    private String providerId;
    private String photoUrl;
}
