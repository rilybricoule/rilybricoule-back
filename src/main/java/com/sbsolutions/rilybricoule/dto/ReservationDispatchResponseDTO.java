package com.sbsolutions.rilybricoule.dto;

import com.sbsolutions.rilybricoule.entity.DispatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDispatchResponseDTO {

    private Long dispatchId;
    private Long reservationId;

    private String description;
    private String category;
    private String subCategory;

    private LocalDate reservationDate;
    private LocalTime reservationTime;

    private DispatchStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;

    private Long clientId;
    private String clientFirstName;
    private String clientLastName;

    private Long prestataireId;
    private String prestataireName;
}