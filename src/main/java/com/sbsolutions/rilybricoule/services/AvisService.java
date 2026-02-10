package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.AvisDTO;
import com.sbsolutions.rilybricoule.entity.Avis;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.AvisRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvisService {
    
    private final AvisRepository avisRepository;
    private final ReservationRepository reservationRepository;
    
    /**
     * Create a review for a reservation.
     * Enforces the rule that only one review per reservation is allowed.
     */
    public Avis createAvis(Long reservationId, Integer rating, String comment, Long prestaireId) 
            throws IllegalArgumentException {
        
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        if (reservation.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found");
        }
        
        // Check if a review already exists for this reservation
        Optional<Avis> existingAvis = avisRepository.findByReservationId(reservationId);
        if (existingAvis.isPresent()) {
            throw new IllegalArgumentException("A review already exists for this reservation");
        }
        
        Reservation res = reservation.get();
        
        Avis avis = Avis.builder()
            .rating(rating)
            .comment(comment)
            .createdDate(LocalDate.now())
            .reservation(res)
            .prestataire(res.getPrestataire())
            .build();
        
        Avis savedAvis = avisRepository.save(avis);
        res.setAvis(savedAvis);
        reservationRepository.save(res);
        
        return savedAvis;
    }
    
    /**
     * Find a review by reservation ID.
     */
    public Optional<Avis> findByReservationId(Long reservationId) {
        return avisRepository.findByReservationId(reservationId);
    }
    
    /**
     * Get all reviews for a prestataire.
     */
    public List<Avis> findByPrestataireId(Long prestataireId) {
        return avisRepository.findByPrestataireIdOrderByCreatedDateDesc(prestataireId);
    }
    
    /**
     * Get average rating for a prestataire.
     */
    public Double getAverageRating(Long prestaireId) {
        List<Avis> avis = findByPrestataireId(prestaireId);
        if (avis.isEmpty()) {
            return null;
        }
        return avis.stream()
            .mapToInt(Avis::getRating)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Map Avis entity to DTO.
     */
    public AvisDTO toDTO(Avis avis) {
        if (avis == null) {
            return null;
        }
        return AvisDTO.builder()
            .id(avis.getId())
            .rating(avis.getRating())
            .comment(avis.getComment())
            .createdDate(avis.getCreatedDate())
            .reservationId(avis.getReservation().getId())
            .prestaireId(avis.getPrestataire().getId())
            .prestaireName(avis.getPrestataire().getName())
            .build();
    }
}
