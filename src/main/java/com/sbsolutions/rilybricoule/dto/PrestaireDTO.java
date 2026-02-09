package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestaireDTO {
    private Long id;
    private String name;
    private String description;
    private String phone;
    private String email;
    private String address;
}
