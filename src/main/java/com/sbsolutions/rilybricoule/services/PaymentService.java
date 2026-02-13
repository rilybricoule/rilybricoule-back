package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.Paiement;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import com.sbsolutions.rilybricoule.repository.PaiementRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Mock payment processor.
     * - If `paymentMethodToken` equals "fail" then simulate a failure.
     * - Otherwise return a successful PaymentResponseDTO.
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Payment request or amount is missing");
        }

        String token = request.getPaymentMethodToken();
        if (token != null && token.equalsIgnoreCase("fail")) {
            throw new PaymentFailedException("Payment was declined by the payment provider");
        }

        // Simulate processing delay / call to external gateway
        String transactionId = UUID.randomUUID().toString();

        return PaymentResponseDTO.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Mock payment succeeded")
            .build();
    }

    /**
     * Process payment and create/update Payment entity.
     * Links payment status to reservation status.
     * On SUCCESS: Reservation status → CONFIRMED
     * On FAILED: Reservation status → CANCELLED
     */
    @Transactional
    public PaymentResponseDTO processPaymentForReservation(Long reservationId, 
                                                          PaymentRequestDTO request,
                                                          String paymentMode) 
            throws IllegalArgumentException, PaymentFailedException {
        
        // Validate reservation exists
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + reservationId));

        // Process payment through payment gateway
        PaymentResponseDTO paymentResponse = processPayment(request);

        // Create payment entity
        Paiement paiement = Paiement.builder()
            .amount(request.getAmount())
            .paymentStatus(Paiement.PaymentStatus.PENDING)
            .paymentMode(paymentMode)
            .reservation(reservation)
            .transactionId(paymentResponse.getTransactionId())
            .build();

        if (paymentResponse.isSuccess()) {
            // Payment successful: update paiement and reservation
            paiement.setPaymentStatus(Paiement.PaymentStatus.SUCCESS);
            paiement.setPaymentDate(LocalDateTime.now());
            
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservation.setPaiement(paiement);
            
            paiementRepository.save(paiement);
            reservationRepository.save(reservation);
        } else {
            // Payment failed: update paiement and reservation
            paiement.setPaymentStatus(Paiement.PaymentStatus.FAILED);
            paiement.setPaymentDate(LocalDateTime.now());
            
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservation.setPaiement(paiement);
            
            paiementRepository.save(paiement);
            reservationRepository.save(reservation);
            
            throw new PaymentFailedException("Payment processing failed: " + paymentResponse.getMessage());
        }

        return paymentResponse;
    }

    /**
     * Retrieve payment for a reservation.
     */
    public Paiement getPaymentByReservationId(Long reservationId) 
            throws IllegalArgumentException {
        return paiementRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for reservation ID: " + reservationId));
    }
}

