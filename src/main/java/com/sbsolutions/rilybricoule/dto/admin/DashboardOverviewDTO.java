package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewDTO {

    private long totalClients;
    private long totalProviders;
    private long pendingProviders;
    private long activeReservations;
    private long completedReservationsToday;
    private BigDecimal monthlyRevenue;
}
