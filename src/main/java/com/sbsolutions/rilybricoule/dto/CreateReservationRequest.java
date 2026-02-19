package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating a new reservation.
 * Contains references to client and prestataire, along with reservation details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {
    
    /**
     * ID of the client making the reservation.
     * Business rule: Client must exist in the system.
     */
    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be a positive number")
    private Long clientId;
    
    /**
     * ID of the prestataire providing the service.
     * Business rule: Prestataire must exist and be active.
     */
    @NotNull(message = "Prestataire ID is required")
    @Positive(message = "Prestataire ID must be a positive number")
    private Long prestaireId;
    
    /**
     * Date of the reservation.
     * Business rule: Must be a future date.
     */
    @NotNull(message = "Reservation date is required")
    @FutureOrPresent(message = "Reservation date must be today or in the future")
    private LocalDate reservationDate;
    
    /**
     * Time of the reservation.
     * Business rule: Combined with reservationDate to determine exact reservation time.
     */
    @NotNull(message = "Reservation time is required")
    private LocalTime reservationTime;
    
    /**
     * Detailed description of the service requested.
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Optional coupon ID for applying discounts.
     * Business rule: If provided, coupon must be active and not expired.
     */
    @Positive(message = "Coupon ID must be a positive number")
    private Long couponId;
}
