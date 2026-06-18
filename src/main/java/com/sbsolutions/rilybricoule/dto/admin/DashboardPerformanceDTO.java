package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPerformanceDTO {

    private double completionRate;
    private BigDecimal avgRevenuePerCompleted;
    private String topProviderName;
    private long topProviderCompletedCount;
    private String topZoneName;
    private long topZoneCompletedCount;
    private List<DashboardRankingItemDTO> providerRanking;
    private List<DashboardRankingItemDTO> zoneRanking;

    private long completedWeekCurrent;
    private long completedWeekPrevious;
    private long completedMonthCurrent;
    private long completedMonthPrevious;
    private long completedYearCurrent;
    private long completedYearPrevious;

    private double completionRateWeekCurrent;
    private double completionRateWeekPrevious;
    private double completionRateMonthCurrent;
    private double completionRateMonthPrevious;
    private double completionRateYearCurrent;
    private double completionRateYearPrevious;

    private BigDecimal avgRevenueWeekCurrent;
    private BigDecimal avgRevenueWeekPrevious;
    private BigDecimal avgRevenueMonthCurrent;
    private BigDecimal avgRevenueMonthPrevious;
    private BigDecimal avgRevenueYearCurrent;
    private BigDecimal avgRevenueYearPrevious;
}
