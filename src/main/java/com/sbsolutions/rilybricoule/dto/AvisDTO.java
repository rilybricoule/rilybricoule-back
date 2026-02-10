package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDate createdDate;
    private Long reservationId;
    private Long prestaireId;
    private String prestaireName;
}
