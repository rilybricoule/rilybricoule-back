package com.sbsolutions.rilybricoule.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDto {
    private Long userId;
    private Boolean enabled;
    private Boolean messageEnabled;
    private Boolean reservationEnabled;
    private Boolean paiementEnabled;
    private Boolean avisEnabled;
}
