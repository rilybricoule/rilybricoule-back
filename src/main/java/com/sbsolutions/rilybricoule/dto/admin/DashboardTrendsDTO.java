package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardTrendsDTO {

    private long clientsWeekCurrent;
    private long clientsWeekPrevious;
    private long clientsMonthCurrent;
    private long clientsMonthPrevious;
    private long clientsYearCurrent;
    private long clientsYearPrevious;

    private long providersWeekCurrent;
    private long providersWeekPrevious;
    private long providersMonthCurrent;
    private long providersMonthPrevious;
    private long providersYearCurrent;
    private long providersYearPrevious;

    private long reservationsWeekCurrent;
    private long reservationsWeekPrevious;
    private long reservationsMonthCurrent;
    private long reservationsMonthPrevious;
    private long reservationsYearCurrent;
    private long reservationsYearPrevious;

    private BigDecimal revenueWeekCurrent;
    private BigDecimal revenueWeekPrevious;
    private BigDecimal revenueMonthCurrent;
    private BigDecimal revenueMonthPrevious;
    private BigDecimal revenueYearCurrent;
    private BigDecimal revenueYearPrevious;
}
