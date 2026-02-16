package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing reservation operations.
 * 
 * Business responsibilities:
 * - Creating new reservations with validation of related entities
 * - Applying valid coupons to reduce reservation cost
 * - Processing payments along with reservations
 * - Querying reservations by client, prestataire, status, or date
 * - Updating reservation status throughout their lifecycle
 * 
 * Reservation lifecycle:
 * 1. PENDING_PAYMENT: Initial state after creation, waiting for payment
 * 2. CONFIRMED: After successful payment, service is confirmed
 * 3. COMPLETED: After service is delivered and payment is settled
 * 4. CANCELLED: If payment fails or reservation is manually cancelled
 * 
 * Coupon application rules:
 * - Only active (non-expired) coupons can be applied
 * - Discount is calculated and applied to final reservation price
 * - Coupon validation is performed at creation time
 * 
 * Payment integration:
 * - Reservations can be created with immediate payment processing
 * - Payment failure results in reservation cancellation
 * - Payment success results in reservation confirmation
 * 
 * Security considerations:
 * - All create/update operations validate entity references exist
 * - Sensitive client/prestataire data is filtered in responses
 * - Transactional consistency ensures payment and reservation are always in sync
 * 
 * @author RilyBricoule Backend Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final PrestaireRepository prestaireRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final PaymentService paymentService;

    /**
     * Create a new reservation from a request.
     * 
     * Business logic:
     * 1. Validates that both client and prestataire exist
     * 2. Creates a new Reservation with PENDING_PAYMENT status
     * 3. If a coupon ID is provided:
     *    a. Retrieves the coupon
     *    b. Validates it is active and not expired
     *    c. Applies the discount to the reservation
     * 4. Calculates the total price after applying discount
     * 5. Persists the reservation to database
     * 
     * @param request the CreateReservationRequest with reservation details
     * @return ReservationResponse with complete reservation information
     * @throws IllegalArgumentException if client or prestataire is not found
     */
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // Validate that client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + request.getClientId()));
        
        // Validate that prestataire exists
        Prestataire prestataire = prestaireRepository.findById(request.getPrestaireId())
                .orElseThrow(() -> new IllegalArgumentException("Prestataire not found with ID: " + request.getPrestaireId()));

        // Create new reservation with initial values
        Reservation reservation = Reservation.builder()
                .reservationDate(request.getReservationDate())
                .reservationTime(request.getReservationTime())
                .description(request.getDescription())
                .client(client)
                .prestataire(prestataire)
                .totalPrice(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .status(Reservation.ReservationStatus.PENDING_PAYMENT)
                .build();

        // Apply coupon if valid
        if (request.getCouponId() != null) {
            couponService.findById(request.getCouponId()).ifPresent(coupon -> {
                if (couponService.isValid(coupon)) {
                    reservation.setCoupon(coupon);
                    reservation.setDiscountAmount(coupon.getDiscountAmount());
                }
            });
        }

        // Calculate total price after discount
        reservation.setTotalPrice(calculateTotalPrice(prestataire, reservation.getDiscountAmount()));

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(savedReservation);
    }

    /**
     * Create a reservation and immediately process payment in one transaction.
     * 
     * Business logic:
     * 1. Creates a new reservation (see createReservation)
     * 2. Validates payment request is provided
     * 3. Sets payment amount to match reservation total price
     * 4. Processes the payment through PaymentService
     * 5. On payment success:
     *    a. Updates reservation status to CONFIRMED
     *    b. Returns updated reservation response
     * 6. On payment failure:
     *    a. Rolls back everything (transactional)
     *    b. Throws PaymentFailedException
     * 
     * IMPORTANT: This is an all-or-nothing operation - both reservation and payment succeed or fail together.
     * 
     * @param request the CreateReservationRequest with reservation details
     * @param paymentRequest the PaymentRequestDTO with payment information
     * @return ReservationResponse with CONFIRMED status if payment succeeds
     * @throws IllegalArgumentException if payment information is missing or invalid
     * @throws PaymentFailedException if payment processing fails
     */
    public ReservationResponse createReservationWithPayment(CreateReservationRequest request, PaymentRequestDTO paymentRequest) {
        // First create the reservation
        ReservationResponse reservationResp = createReservation(request);
        
        // Retrieve the full reservation entity
        Reservation reservation = reservationRepository.findById(reservationResp.getId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        // Validate payment information is provided
        if (paymentRequest == null) throw new IllegalArgumentException("Payment information is required");
        
        // Ensure payment amount matches reservation total
        paymentRequest.setAmount(reservation.getTotalPrice());
        if (paymentRequest.getCurrency() == null) paymentRequest.setCurrency("EUR");

        // Process the payment
        PaymentResponseDTO paymentResponse = paymentService.processPayment(paymentRequest);
        
        if (paymentResponse.isSuccess()) {
            // Payment successful: update reservation to CONFIRMED
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            return ReservationResponse.fromEntity(reservation);
        } else {
            throw new PaymentFailedException("Payment failed");
        }
    }

    /**
     * Retrieve a specific reservation by ID.
     * 
     * @param id the reservation ID
     * @return ReservationResponse with complete reservation details
     * @throws IllegalArgumentException if reservation is not found
     */
    public ReservationResponse getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(ReservationResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + id));
    }

    /**
     * Retrieve all reservations made by a specific client.
     * 
     * Business rule: Clients can view their own reservations.
     * 
     * @param clientId the client's ID
     * @return List of ReservationResponse objects for the client
     */
    public List<ReservationResponse> getClientReservations(Long clientId) {
        return reservationRepository.findByClient_Id(clientId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all reservations for a specific service provider (prestataire).
     * 
     * Business rule: Prestataires can view all their upcoming reservations.
     * 
     * @param prestataireId the prestataire's ID
     * @return List of ReservationResponse objects assigned to the prestataire
     */
    public List<ReservationResponse> getPrestaireReservations(Long prestataireId) {
        return reservationRepository.findByPrestataire_Id(prestataireId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update the status of an existing reservation.
     * 
     * Business logic:
     * 1. Validates the new status is a valid ReservationStatus enum value
     * 2. Updates the reservation status
     * 3. Persists the change
     * 
     * Valid status transitions:
     * - PENDING_PAYMENT → CONFIRMED (payment success)
     * - PENDING_PAYMENT → CANCELLED (payment failure)
     * - CONFIRMED → COMPLETED (service finished)
     * - Any status → CANCELLED (manual cancellation)
     * 
     * @param id the reservation ID
     * @param status the new status as string (e.g., "CONFIRMED", "COMPLETED")
     * @return ReservationResponse with updated status
     * @throws IllegalArgumentException if reservation not found or status is invalid
     */
    public ReservationResponse updateReservationStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + id));

        try {
            Reservation.ReservationStatus newStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            reservation.setStatus(newStatus);
            reservationRepository.save(reservation);
            return ReservationResponse.fromEntity(reservation);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * Cancel an existing reservation.
     * 
     * @param id the reservation ID to cancel
     * @return ReservationResponse with CANCELLED status
     * @throws IllegalArgumentException if reservation is not found
     */
    public ReservationResponse cancelReservation(Long id) {
        return updateReservationStatus(id, "CANCELLED");
    }

    /**
     * Find all reservations with a specific status.
     * 
     * Useful for querying reservations in their various lifecycle stages.
     * 
     * @param status the ReservationStatus to filter by
     * @return List of ReservationResponse objects with the given status
     */
    public List<ReservationResponse> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Find all reservations scheduled for a specific date.
     * 
     * Business rule: Useful for daily scheduling and resource planning.
     * 
     * @param date the reservation date to filter by
     * @return List of ReservationResponse objects scheduled for the given date
     */
    public List<ReservationResponse> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Calculate the total price for a reservation.
     * 
     * Business logic:
     * 1. Starts with a base price (currently hardcoded to 100.00 EUR)
     * 2. Subtracts any applicable discount
     * 3. Returns the final price
     * 
     * Note: In production, the base price should come from Service or Prestataire pricing.
     * 
     * Formula: totalPrice = basePrice - discount
     * 
     * @param prestataire the prestataire providing the service
     * @param discount the discount amount to apply (if any)
     * @return the final total price after discount
     */
    private BigDecimal calculateTotalPrice(Prestataire prestataire, BigDecimal discount) {
        BigDecimal basePrice = new BigDecimal("100.00");
        return (discount != null && discount.compareTo(BigDecimal.ZERO) > 0)
                ? basePrice.subtract(discount)
                : basePrice;
    }
}
