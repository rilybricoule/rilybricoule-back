package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.AvisDTO;
import com.sbsolutions.rilybricoule.entity.Avis;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Avis (Review) entity and AvisDTO.
 * Handles mapping of review information for API requests and responses.
 */
@Component
public class AvisMapper {

    /**
     * Convert Avis entity to AvisDTO.
     * Extracts all relevant review information for API responses.
     * Includes prestataire information for display purposes.
     * 
     * @param avis the Avis entity to convert
     * @return AvisDTO with all relevant fields, or null if input is null
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
            .reservationId(avis.getReservation() != null ? avis.getReservation().getId() : null)
            .prestaireId(avis.getPrestataire() != null ? avis.getPrestataire().getId() : null)
            .prestaireName(avis.getPrestataire() != null ? avis.getPrestataire().getName() : null)
            .build();
    }

    /**
     * Convert AvisDTO to Avis entity.
     * Used for creating new reviews from API requests.
     * Note: Reservation and Prestataire relationships must be set separately.
     * 
     * @param dto the AvisDTO with review data
     * @return Avis entity with populated fields, or null if input is null
     */
    public Avis toEntity(AvisDTO dto) {
        if (dto == null) {
            return null;
        }

        return Avis.builder()
            .rating(dto.getRating())
            .comment(dto.getComment())
            .createdDate(dto.getCreatedDate())
            .build();
        // Note: reservation and prestataire relationships must be set separately
    }

    /**
     * Update existing Avis entity with new data.
     * Preserves ID, createdDate, and relationships while updating review content.
     * 
     * @param dto the AvisDTO with updated data
     * @param avis the existing Avis entity to update
     * @return the updated Avis entity
     */
    public Avis updateEntity(AvisDTO dto, Avis avis) {
        if (dto == null) {
            return avis;
        }

        if (dto.getRating() != null) {
            avis.setRating(dto.getRating());
        }
        if (dto.getComment() != null) {
            avis.setComment(dto.getComment());
        }

        return avis;
    }
}
