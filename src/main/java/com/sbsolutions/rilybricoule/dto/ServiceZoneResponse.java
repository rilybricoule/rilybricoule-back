package com.sbsolutions.rilybricoule.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceZoneResponse {
    private Long id;
    private Double centerLat;
    private Double centerLng;
    private Integer radiusMeters;
    private String label;
}