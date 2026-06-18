package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardExtraKpiDTO {

    private double cancellationRate;
    private double paymentFailureRate;

    private double paymentFailureRateWeekCurrent;
    private double paymentFailureRateWeekPrevious;
    private double paymentFailureRateMonthCurrent;
    private double paymentFailureRateMonthPrevious;
    private double paymentFailureRateYearCurrent;
    private double paymentFailureRateYearPrevious;

    private long activeReservationsWeekCurrent;
    private long activeReservationsWeekPrevious;
    private long activeReservationsMonthCurrent;
    private long activeReservationsMonthPrevious;
    private long activeReservationsYearCurrent;
    private long activeReservationsYearPrevious;

    private double cancellationRateWeekCurrent;
    private double cancellationRateWeekPrevious;
    private double cancellationRateMonthCurrent;
    private double cancellationRateMonthPrevious;
    private double cancellationRateYearCurrent;
    private double cancellationRateYearPrevious;
}
