package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminReservationDTO;
import com.sbsolutions.rilybricoule.entity.Paiement;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @Transactional(readOnly = true)
    public List<AdminReservationDTO> getAll() {
        return reservationRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Reservation::getReservationDate,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminReservationDTO updateStatus(Long id, String status) {
        reservationService.updateReservationStatus(id, status);
        Reservation updated = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return toDto(updated);
    }

    @Transactional
    public AdminReservationDTO cancel(Long id) {
        reservationService.cancelReservation(id);
        Reservation updated = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return toDto(updated);
    }

    @Transactional
    public AdminReservationDTO updateNote(Long id, String note) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Your Reservation entity does not have adminNote yet.
        // Add it later if you want to persist notes.

        return toDto(reservation);
    }

    private AdminReservationDTO toDto(Reservation reservation) {
        Paiement paiement = reservation.getPaiement();

        return AdminReservationDTO.builder()
                .id(reservation.getId())

                .clientId(reservation.getClient() != null ? reservation.getClient().getId() : null)
                .clientName(reservation.getClient() != null
                        ? reservation.getClient().getFirstName() + " " + reservation.getClient().getLastName()
                        : null)
                .clientEmail(reservation.getClient() != null ? reservation.getClient().getEmail() : null)

                .providerId(reservation.getPrestataire() != null ? reservation.getPrestataire().getId() : null)
                .providerName(reservation.getPrestataire() != null ? reservation.getPrestataire().getName() : null)
                .providerEmail(reservation.getPrestataire() != null ? reservation.getPrestataire().getEmail() : null)

                .serviceId(null)
                .serviceName(reservation.getDescription() != null ? reservation.getDescription() : "Reservation #" + reservation.getId())
                .category(null)

                .scheduledDate(reservation.getReservationDate())
                .scheduledTime(reservation.getReservationTime())

                .amount(reservation.getTotalPrice())
                .discountAmount(reservation.getDiscountAmount())

                .status(reservation.getStatus() != null ? reservation.getStatus().name() : null)

                .paymentId(paiement != null ? paiement.getId() : null)
                .paymentMethod(paiement != null ? paiement.getPaymentMode() : null)
                .paymentStatus(paiement != null && paiement.getPaymentStatus() != null
                        ? paiement.getPaymentStatus().name()
                        : null)

                .address(reservation.getClient() != null ? reservation.getClient().getAddress() : null)
                .latitude(null)
                .longitude(null)

                .cancelledAt(reservation.getCancelledAt())
                .cancelReason(null)
                .adminNote(null)

                .createdAt(null)
                .updatedAt(null)
                .build();
    }
}