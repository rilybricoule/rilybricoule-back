package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AdminReservationDTO {
    private Long id;

    private Long clientId;
    private String clientName;
    private String clientEmail;

    private Long providerId;
    private String providerName;
    private String providerEmail;

    private Long serviceId;
    private String serviceName;
    private String category;

    private LocalDate scheduledDate;
    private LocalTime scheduledTime;

    private BigDecimal amount;
    private BigDecimal discountAmount;

    private String status;

    private Long paymentId;
    private String paymentMethod;
    private String paymentStatus;

    private String address;
    private Double latitude;
    private Double longitude;

    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
