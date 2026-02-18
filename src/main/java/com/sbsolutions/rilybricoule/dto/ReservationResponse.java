package com.sbsolutions.rilybricoule.dto;

import com.sbsolutions.rilybricoule.entity.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for reservation details.
 * Contains complete reservation information including related entities (client, prestataire, coupon, review).
 * Populated from Reservation entity via mapper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    
    /**
     * Unique reservation identifier.
     */
    @NotNull(message = "Reservation ID is required")
    private Long id;
    
    /**
     * Reservation date.
     */
    @NotNull(message = "Reservation date is required")
    private LocalDate reservationDate;
    
    /**
     * Reservation time.
     */
    @NotNull(message = "Reservation time is required")
    private LocalTime reservationTime;
    
    /**
     * Description of the service requested.
     */
    private String description;
    
    /**
     * Total price for the reservation.
     * Business rule: This is the final price after applying any discounts.
     */
    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;
    
    /**
     * Discount amount applied (if any).
     * Business rule: This is the discount from the applied coupon, if applicable.
     */
    private BigDecimal discountAmount;
    
    /**
     * Status of the reservation (PENDING_PAYMENT, CONFIRMED, COMPLETED, CANCELLED).
     * Business rule: Status changes are determined by payment and service completion.
     */
    @NotNull(message = "Reservation status is required")
    private String status;
    
    /**
     * Client information (non-sensitive fields only).
     * Nested DTO for client details.
     */
    private ClientDTO client;
    
    /**
     * Prestataire (service provider) information.
     * Nested DTO for prestataire details.
     */
    private PrestaireDTO prestataire;
    
    /**
     * Applied coupon information (if any).
     * Nested DTO for coupon details.
     */
    private CouponDTO coupon;
    
    /**
     * Review/Avis information (if any).
     * Nested DTO for review details provided after service completion.
     */
    private AvisDTO avis;
    
    /**
     * Map Reservation entity to ReservationResponse DTO.
     * Extracts all relevant information and builds nested DTOs.
     * 
     * @param reservation the Reservation entity to map
     * @return populated ReservationResponse DTO
     */
    public static ReservationResponse fromEntity(com.sbsolutions.rilybricoule.entity.Reservation reservation) {
        return ReservationResponse.builder()
            .id(reservation.getId())
            .reservationDate(reservation.getReservationDate())
            .reservationTime(reservation.getReservationTime())
            .description(reservation.getDescription())
            .totalPrice(reservation.getTotalPrice())
            .discountAmount(reservation.getDiscountAmount())
            .status(reservation.getStatus() != null ? reservation.getStatus().name() : null)
            .client(ClientDTO.builder()
                .id(reservation.getClient().getId())
                .firstName(reservation.getClient().getFirstName())
                .lastName(reservation.getClient().getLastName())
                .email(reservation.getClient().getEmail())
                .phone(reservation.getClient().getPhone())
                .address(reservation.getClient().getAddress())
                .build())
            .prestataire(PrestaireDTO.builder()
                .id(reservation.getPrestataire().getId())
                .name(reservation.getPrestataire().getName())
                .description(reservation.getPrestataire().getDescription())
                .phone(reservation.getPrestataire().getPhone())
                .email(reservation.getPrestataire().getEmail())
                .address(reservation.getPrestataire().getAddress())
                .build())
            .coupon(reservation.getCoupon() != null ? CouponDTO.builder()
                .id(reservation.getCoupon().getId())
                .code(reservation.getCoupon().getCode())
                .description(reservation.getCoupon().getDescription())
                .discountAmount(reservation.getCoupon().getDiscountAmount())
                .discountPercentage(reservation.getCoupon().getDiscountPercentage())
                .expiryDate(reservation.getCoupon().getExpiryDate())
                .active(reservation.getCoupon().getActive())
                .build() : null)
            .avis(reservation.getAvis() != null ? AvisDTO.builder()
                .id(reservation.getAvis().getId())
                .rating(reservation.getAvis().getRating())
                .comment(reservation.getAvis().getComment())
                .createdDate(reservation.getAvis().getCreatedDate())
                .reservationId(reservation.getAvis().getReservation().getId())
                .prestaireId(reservation.getAvis().getPrestataire().getId())
                .prestaireName(reservation.getAvis().getPrestataire().getName())
                .build() : null)
            .build();
    }
}
