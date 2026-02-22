package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.dto.ReservationPaymentRequest;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import com.sbsolutions.rilybricoule.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API Controller for reservation management.
 * 
 * Endpoints:
 * - POST /api/reservations - Create a new reservation
 * - POST /api/reservations/with-payment - Create reservation and process payment
 * - GET /api/reservations/{id} - Get reservation by ID
 * - GET /api/reservations/client/{clientId} - Get reservations for a client
 * - GET /api/reservations/prestataire/{prestaireId} - Get reservations for a prestataire
 * - PATCH /api/reservations/{id}/status - Update reservation status
 * - POST /api/reservations/{id}/cancel - Cancel a reservation
 * - GET /api/reservations/by-date - Get reservations by date
 * 
 * Uses DTOs for request/response for API consistency and security.
 * Request body validation is automatically enforced via @Valid annotation.
 * Exception handling is delegated to RestExceptionHandler.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * Create a new reservation.
     * 
     * Business logic:
     * - Validates client and prestataire exist
     * - Applies valid coupon if provided
     * - Sets status to PENDING_PAYMENT
     * 
     * @param request the CreateReservationRequest (validated)
     * @return ResponseEntity with created ReservationResponse and 201 CREATED status
     * @throws IllegalArgumentException if client or prestataire not found
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create a reservation and immediately process payment in one transaction.
     * 
     * Business logic:
     * - Creates a new reservation (see createReservation)
     * - Processes payment with the reservation amount
     * - On success: Reservation status = CONFIRMED
     * - On failure: Reservation is rolled back and exception is thrown
     * 
     * @param request the ReservationPaymentRequest with both reservation and payment data
     * @return ResponseEntity with ReservationResponse (CONFIRMED status) and 201 CREATED
     * @throws IllegalArgumentException if required data is missing
     * @throws PaymentFailedException if payment processing fails
     */
    @PostMapping("/with-payment")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservationWithPayment(
            @Valid @RequestBody ReservationPaymentRequest request) {
        ReservationResponse response = reservationService.createReservationWithPayment(
            request.getReservation(), request.getPayment());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get a specific reservation by ID.
     * 
     * @param id the reservation ID
     * @return ResponseEntity with ReservationResponse
     * @throws IllegalArgumentException if reservation not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        ReservationResponse response = reservationService.getReservation(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all reservations made by a specific client.
     * 
     * @param clientId the client ID
     * @return ResponseEntity with list of ReservationResponse objects
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getClientReservations(@PathVariable Long clientId) {
        List<ReservationResponse> responses = reservationService.getClientReservations(clientId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get all reservations assigned to a specific prestataire (service provider).
     * 
     * @param prestaireId the prestataire ID
     * @return ResponseEntity with list of ReservationResponse objects
     */
    @GetMapping("/prestataire/{prestaireId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getPrestaireReservations(@PathVariable Long prestaireId) {
        List<ReservationResponse> responses = reservationService.getPrestaireReservations(prestaireId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Update the status of a reservation.
     * 
     * Valid status values: PENDING_PAYMENT, CONFIRMED, COMPLETED, CANCELLED
     * 
     * @param id the reservation ID
     * @param status the new status (enum value as string)
     * @return ResponseEntity with updated ReservationResponse
     * @throws IllegalArgumentException if reservation not found or status is invalid
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ReservationResponse response = reservationService.updateReservationStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a reservation.
     * 
     * Sets the reservation status to CANCELLED.
     * 
     * @param id the reservation ID
     * @return ResponseEntity with updated ReservationResponse (CANCELLED status)
     * @throws IllegalArgumentException if reservation not found
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        ReservationResponse response = reservationService.cancelReservation(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all reservations scheduled for a specific date.
     * 
     * @param date the reservation date to filter by
     * @return ResponseEntity with list of ReservationResponse objects for the date
     */
    @GetMapping("/by-date")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getReservationsByDate(
            @RequestParam LocalDate date) {
        List<ReservationResponse> responses = reservationService.getReservationsByDate(date);
        return ResponseEntity.ok(responses);
    }
}
