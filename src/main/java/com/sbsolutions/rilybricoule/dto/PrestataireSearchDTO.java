package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrestataireSearchDTO {

    private Long id;
    private String name;

    private Double distanceKm;
    private Double score;
    private Double averageRating;
    private BigDecimal minPrice;
}

