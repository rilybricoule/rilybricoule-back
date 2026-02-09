package com.sbsolutions.rilybricoule.dto;

import com.sbsolutions.rilybricoule.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private String description;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private String status;
    private ClientDTO client;
    private PrestaireDTO prestataire;
    private CouponDTO coupon;
    private AvisDTO avis;
    
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
