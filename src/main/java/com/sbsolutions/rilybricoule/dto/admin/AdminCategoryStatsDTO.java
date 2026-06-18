package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCategoryStatsDTO {

    private long totalCategories;

    private long totalSubCategories;

    private long totalServices;

    private long totalProviders;
}