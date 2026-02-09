package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.AvisDTO;
import com.sbsolutions.rilybricoule.entity.Avis;
import com.sbsolutions.rilybricoule.services.AvisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
public class AvisController {
    
    private final AvisService avisService;
    
    /**
     * Create a review for a reservation.
     * Enforces that only one review per reservation is allowed.
     */
    @PostMapping
    public ResponseEntity<?> createAvis(
            @RequestParam Long reservationId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam Long prestaireId) {
        try {
            Avis avis = avisService.createAvis(reservationId, rating, comment, prestaireId);
            return ResponseEntity.status(HttpStatus.CREATED).body(avisService.toDTO(avis));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get a review by reservation ID.
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getAvisByReservation(@PathVariable Long reservationId) {
        var avis = avisService.findByReservationId(reservationId);
        if (avis.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No review found for this reservation");
        }
        return ResponseEntity.ok(avisService.toDTO(avis.get()));
    }
    
    /**
     * Get all reviews for a prestataire.
     */
    @GetMapping("/prestataire/{prestaireId}")
    public ResponseEntity<List<AvisDTO>> getAvisByPrestataire(@PathVariable Long prestaireId) {
        List<Avis> avisList = avisService.findByPrestaireId(prestaireId);
        List<AvisDTO> response = avisList.stream()
            .map(avisService::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get average rating for a prestataire.
     */
    @GetMapping("/prestataire/{prestaireId}/average-rating")
    public ResponseEntity<?> getAverageRating(@PathVariable Long prestaireId) {
        Double averageRating = avisService.getAverageRating(prestaireId);
        if (averageRating == null) {
            return ResponseEntity.ok("No reviews yet");
        }
        return ResponseEntity.ok("Average rating: " + String.format("%.2f", averageRating));
    }
}
