package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceZoneRequest {

    @NotNull
    private Double centerLat;

    @NotNull
    private Double centerLng;

    @NotNull
    @Min(100)
    @Max(200000)
    private Integer radiusMeters;

    private String label;
}