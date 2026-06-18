package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPaymentDTO {
    private Long id;
    private Long reservationId;

    private Long clientId;
    private String clientName;
    private String clientEmail;

    private Long providerId;
    private String providerName;
    private String providerEmail;

    private String serviceName;
    private String category;

    private BigDecimal amount;
    private BigDecimal commission;
    private BigDecimal providerPayout;

    private String paymentMethod;
    private String paymentStatus;
    private String payoutStatus;
    private String commissionStatus;

    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}