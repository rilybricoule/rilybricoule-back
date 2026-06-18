package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardQualityTrendsDTO {

    private long verifiedWeekCurrent;
    private long verifiedWeekPrevious;
    private long verifiedMonthCurrent;
    private long verifiedMonthPrevious;
    private long verifiedYearCurrent;
    private long verifiedYearPrevious;

    private long pendingWeekCurrent;
    private long pendingWeekPrevious;
    private long pendingMonthCurrent;
    private long pendingMonthPrevious;
    private long pendingYearCurrent;
    private long pendingYearPrevious;
}
