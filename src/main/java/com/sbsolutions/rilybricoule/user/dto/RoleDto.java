package com.sbsolutions.rilybricoule.user.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {


    private Long id;

    private String name;

    private String createdBy;

    private LocalDateTime createdDate;

}
