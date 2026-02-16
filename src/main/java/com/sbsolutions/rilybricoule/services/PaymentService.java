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

/**
 * Service class for managing payment operations.
 * 
 * Business responsibilities:
 * - Processing payments through payment gateway (mock implementation)
 * - Creating and updating payment records (Paiement entities)
 * - Linking payment status to reservation status
 * - Validating payment requests
 * 
 * Payment flow:
 * 1. Payment request is validated for required fields
 * 2. If paymentMethodToken == "fail", payment is declined (mock simulation)
 * 3. On success: Paiement status = SUCCESS, Reservation status = CONFIRMED
 * 4. On failure: Paiement status = FAILED, Reservation status = CANCELLED
 * 
 * Security considerations:
 * - Payment method tokens should be tokenized in production (no stored credentials)
 * - Never expose payment method information in API responses
 * - All payment operations are transactional to ensure consistency
 * - Payment data is handled with care for PCI compliance (in production)
 * 
 * @author RilyBricoule Backend Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Process a payment request through the payment gateway.
     * 
     * This is a MOCK implementation for development/testing:
     * - If paymentMethodToken == "fail": simulate payment decline
     * - Otherwise: simulate successful payment processing
     * 
     * In production, this would integrate with a real payment gateway (Stripe, PayPal, etc.)
     * and handle various failure scenarios (declined card, insufficient funds, etc.)
     * 
     * @param request the payment request containing amount, currency, and payment method token
     * @return PaymentResponseDTO with success status and transaction ID
     * @throws IllegalArgumentException if payment request or amount is null/missing
     * @throws PaymentFailedException if payment token equals "fail" (mock decline)
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Payment request or amount is missing");
        }

        String token = request.getPaymentMethodToken();
        if (token != null && token.equalsIgnoreCase("fail")) {
            throw new PaymentFailedException("Payment was declined by the payment provider");
        }

        // In mock mode: generate transaction ID and return success
        String transactionId = UUID.randomUUID().toString();

        return PaymentResponseDTO.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Mock payment succeeded")
            .build();
    }

    /**
     * Process payment for a specific reservation and update statuses accordingly.
     * 
     * Business logic:
     * 1. Validates that the reservation exists
     * 2. Processes the payment request
     * 3. Creates/updates Paiement entity with payment details
     * 4. On SUCCESS:
     *    - Sets Paiement status = SUCCESS with payment date
     *    - Updates Reservation status = CONFIRMED
     *    - Links payment to reservation
     * 5. On FAILURE:
     *    - Sets Paiement status = FAILED with current timestamp
     *    - Updates Reservation status = CANCELLED
     *    - Throws PaymentFailedException
     * 
     * IMPORTANT: This method is transactional. All database changes are atomic -
     * either all succeed or all fail together.
     * 
     * @param reservationId the ID of the reservation being paid for
     * @param request the payment request with amount and payment method
     * @param paymentMode the payment mode (e.g., "CARD", "BANK_TRANSFER"). 
     *                   Used for reference and reporting.
     * @return PaymentResponseDTO with transaction details on success
     * @throws IllegalArgumentException if reservation is not found
     * @throws PaymentFailedException if payment processing fails
     */
    @Transactional
    public PaymentResponseDTO processPaymentForReservation(Long reservationId, 
                                                          PaymentRequestDTO request,
                                                          String paymentMode) 
            throws IllegalArgumentException, PaymentFailedException {
        
        // Validate that the reservation exists
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + reservationId));

        // Process payment through payment gateway (mock or real)
        PaymentResponseDTO paymentResponse = processPayment(request);

        // Create payment entity with current request data
        Paiement paiement = Paiement.builder()
            .amount(request.getAmount())
            .paymentStatus(Paiement.PaymentStatus.PENDING)
            .paymentMode(paymentMode)
            .reservation(reservation)
            .transactionId(paymentResponse.getTransactionId())
            .build();

        if (paymentResponse.isSuccess()) {
            // Payment successful: update paiement and reservation to CONFIRMED
            paiement.setPaymentStatus(Paiement.PaymentStatus.SUCCESS);
            paiement.setPaymentDate(LocalDateTime.now());
            
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservation.setPaiement(paiement);
            
            paiementRepository.save(paiement);
            reservationRepository.save(reservation);
        } else {
            // Payment failed: update paiement and reservation to CANCELLED
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
     * Retrieve the payment record associated with a reservation.
     * 
     * @param reservationId the ID of the reservation
     * @return the Paiement entity associated with the reservation
     * @throws IllegalArgumentException if no payment is found for the given reservation
     */
    public Paiement getPaymentByReservationId(Long reservationId) 
            throws IllegalArgumentException {
        return paiementRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for reservation ID: " + reservationId));
    }
}

