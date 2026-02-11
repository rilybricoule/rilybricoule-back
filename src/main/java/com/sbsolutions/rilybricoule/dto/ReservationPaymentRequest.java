package com.sbsolutions.rilybricoule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPaymentRequest {
    private CreateReservationRequest reservation;
    private PaymentRequestDTO payment;
}
