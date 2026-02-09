package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.CreateReservationRequest;
import com.sbsolutions.rilybricoule.dto.ReservationResponse;
import com.sbsolutions.rilybricoule.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * Create a new reservation.
     * Accepts optional coupon ID which is validated and applied if valid.
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            ReservationResponse response = reservationService.createReservation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get a reservation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        try {
            ReservationResponse response = reservationService.getReservation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get all reservations for a specific client.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReservationResponse>> getClientReservations(@PathVariable Long clientId) {
        List<ReservationResponse> responses = reservationService.getClientReservations(clientId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get all reservations for a specific prestataire.
     */
    @GetMapping("/prestataire/{prestaireId}")
    public ResponseEntity<List<ReservationResponse>> getPrestaireReservations(@PathVariable Long prestaireId) {
        List<ReservationResponse> responses = reservationService.getPrestaireReservations(prestaireId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Update reservation status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            ReservationResponse response = reservationService.updateReservationStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Cancel a reservation.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            ReservationResponse response = reservationService.cancelReservation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get reservations by date.
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<ReservationResponse>> getReservationsByDate(
            @RequestParam LocalDate date) {
        List<ReservationResponse> responses = reservationService.getReservationsByDate(date);
        return ResponseEntity.ok(responses);
    }
}
