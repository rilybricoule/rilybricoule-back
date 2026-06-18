package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminServiceOfferDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Boolean active;
    private String moderationStatus;
    private String moderationNote;
    private Long providerId;
    private String providerName;
    private String providerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}