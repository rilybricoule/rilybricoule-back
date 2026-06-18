package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPaymentStatusDTO {

    private long paid;
    private long pending;
    private long refunded;
    private long failed;
    private long total;
}
