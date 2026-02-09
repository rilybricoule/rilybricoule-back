package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {
    private Long clientId;
    private Long prestaireId;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private String description;
    private Long couponId;
}
