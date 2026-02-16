package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.*;
import com.sbsolutions.rilybricoule.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Reservation entity and ReservationDTO objects.
 * Handles mapping of complex nested structures including related entities.
 * Ensures sensitive information is not exposed in API responses.
 */
@Component
@RequiredArgsConstructor
public class ReservationMapper {

    private final ClientMapper clientMapper;
    private final PrestaireMapper prestaireMapper;
    private final CouponMapper couponMapper;
    private final AvisMapper avisMapper;

    /**
     * Convert Reservation entity to ReservationResponse DTO.
     * Maps all nested entities using their respective mappers.
     * 
     * @param reservation the Reservation entity to convert
     * @return ReservationResponse with all related data, or null if input is null
     */
    public ReservationResponse toResponseDTO(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return ReservationResponse.builder()
            .id(reservation.getId())
            .reservationDate(reservation.getReservationDate())
            .reservationTime(reservation.getReservationTime())
            .description(reservation.getDescription())
            .totalPrice(reservation.getTotalPrice())
            .discountAmount(reservation.getDiscountAmount())
            .status(reservation.getStatus() != null ? reservation.getStatus().name() : null)
            .client(clientMapper.toDTO(reservation.getClient()))
            .prestataire(prestaireMapper.toDTO(reservation.getPrestataire()))
            .coupon(couponMapper.toDTO(reservation.getCoupon()))
            .avis(avisMapper.toDTO(reservation.getAvis()))
            .build();
    }

    /**
     * Convert CreateReservationRequest DTO to Reservation entity.
     * Creates a new reservation with initial values from the request.
     * Note: Client, Prestataire, and Coupon relationships must be set separately.
     * Status is initialized to PENDING_PAYMENT.
     * 
     * @param request the CreateReservationRequest DTO
     * @return Reservation entity with request data, or null if input is null
     */
    public Reservation toEntity(CreateReservationRequest request) {
        if (request == null) {
            return null;
        }

        return Reservation.builder()
            .reservationDate(request.getReservationDate())
            .reservationTime(request.getReservationTime())
            .description(request.getDescription())
            .totalPrice(null) // Must be calculated after setting coupon
            .discountAmount(null) // Will be set from coupon
            .status(Reservation.ReservationStatus.PENDING_PAYMENT)
            .build();
        // Note: client, prestataire, coupon relationships must be set separately
    }

    /**
     * Update existing Reservation with new data.
     * Preserves ID and other system-generated fields.
     * 
     * @param request the CreateReservationRequest with updated data
     * @param reservation the existing Reservation entity to update
     * @return the updated Reservation entity
     */
    public Reservation updateEntity(CreateReservationRequest request, Reservation reservation) {
        if (request == null) {
            return reservation;
        }

        if (request.getReservationDate() != null) {
            reservation.setReservationDate(request.getReservationDate());
        }
        if (request.getReservationTime() != null) {
            reservation.setReservationTime(request.getReservationTime());
        }
        if (request.getDescription() != null) {
            reservation.setDescription(request.getDescription());
        }

        return reservation;
    }
}
