package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combined request DTO for creating a reservation with immediate payment processing.
 * 
 * This DTO is used when clients want to create a reservation and pay for it
 * in a single atomic transaction (all-or-nothing operation).
 * 
 * Business rule: Both reservation and payment must succeed or the entire
 * operation is rolled back for transactional consistency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPaymentRequest {
    
    /**
     * Reservation details.
     * Business rule: Must be provided and valid.
     * Validated via @Valid cascade validation.
     */
    @NotNull(message = "Reservation information is required")
    @Valid
    private CreateReservationRequest reservation;
    
    /**
     * Payment details.
     * Business rule: Must be provided and valid.
     * Payment amount will be automatically set to match reservation total price.
     * Validated via @Valid cascade validation.
     */
    @NotNull(message = "Payment information is required")
    @Valid
    private PaymentRequestDTO payment;
}
