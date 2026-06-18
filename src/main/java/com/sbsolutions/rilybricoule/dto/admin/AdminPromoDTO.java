package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminPromoDTO {

    private Long id;

    private String code;

    private String title;

    private String description;

    private BigDecimal discountAmount;

    private Integer discountPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private String targetAudience;

    private Integer maxUsage;

    private Integer currentUsage;

    private Boolean active;

    private LocalDateTime createdAt;
}
