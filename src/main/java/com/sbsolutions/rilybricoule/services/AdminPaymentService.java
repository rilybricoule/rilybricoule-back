package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminPaymentDTO;
import com.sbsolutions.rilybricoule.entity.Paiement;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPaymentService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.15");

    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public List<AdminPaymentDTO> getAll(Long reservationId) {
        List<Paiement> payments = reservationId == null
                ? paiementRepository.findAllByOrderByCreatedAtDesc()
                : paiementRepository.findAllByReservation_IdOrderByCreatedAtDesc(reservationId);

        return payments.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminPaymentDTO updatePaymentStatus(Long id, String status) {
        Paiement paiement = findPayment(id);
        paiement.setPaymentStatus(Paiement.PaymentStatus.valueOf(status));
        return toDto(paiementRepository.save(paiement));
    }

    @Transactional
    public AdminPaymentDTO updatePayoutStatus(Long id, String status) {
        Paiement paiement = findPayment(id);
        paiement.setPayoutStatus(Paiement.PayoutStatus.valueOf(status));
        return toDto(paiementRepository.save(paiement));
    }

    @Transactional
    public AdminPaymentDTO updateCommissionStatus(Long id, String status) {
        Paiement paiement = findPayment(id);
        paiement.setCommissionStatus(Paiement.CommissionStatus.valueOf(status));
        return toDto(paiementRepository.save(paiement));
    }

    private Paiement findPayment(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable"));
    }

    private AdminPaymentDTO toDto(Paiement paiement) {
        Reservation reservation = paiement.getReservation();

        BigDecimal amount = paiement.getAmount() == null ? BigDecimal.ZERO : paiement.getAmount();
        BigDecimal commission = amount.multiply(COMMISSION_RATE);
        BigDecimal providerPayout = amount.subtract(commission);

        return AdminPaymentDTO.builder()
                .id(paiement.getId())
                .reservationId(reservation != null ? reservation.getId() : null)

                .clientId(reservation != null && reservation.getClient() != null ? reservation.getClient().getId() : null)
                .clientName(reservation != null && reservation.getClient() != null
                        ? reservation.getClient().getFirstName() + " " + reservation.getClient().getLastName()
                        : "Client")
                .clientEmail(reservation != null && reservation.getClient() != null ? reservation.getClient().getEmail() : null)

                .providerId(reservation != null && reservation.getPrestataire() != null ? reservation.getPrestataire().getId() : null)
                .providerName(reservation != null && reservation.getPrestataire() != null
                        ? reservation.getPrestataire().getFirstName() + " " + reservation.getPrestataire().getLastName()
                        : "Prestataire")
                .providerEmail(reservation != null && reservation.getPrestataire() != null ? reservation.getPrestataire().getEmail() : null)

                .serviceName(reservation != null ? "Réservation #" + reservation.getId() : "Paiement")
                .category("")

                .amount(amount)
                .commission(commission)
                .providerPayout(providerPayout)

                .paymentMethod(paiement.getPaymentMode())
                .paymentStatus(paiement.getPaymentStatus().name())
                .payoutStatus(paiement.getPayoutStatus().name())
                .commissionStatus(paiement.getCommissionStatus().name())

                .transactionId(paiement.getTransactionId())
                .paymentDate(paiement.getPaymentDate())
                .createdAt(paiement.getCreatedAt())
                .updatedAt(paiement.getUpdatedAt())
                .build();
    }
}