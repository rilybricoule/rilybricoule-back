package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Prestataire entity and PrestaireDTO.
 * Ensures clean separation between database entities and API request/response objects.
 * Excludes sensitive fields not needed in API responses.
 */
@Component
public class PrestaireMapper {

    /**
     * Convert Prestataire entity to PrestaireDTO.
     * Extracts only publicly relevant prestataire information for API responses.
     * Excludes sensitive data like CIN (national identification number).
     * 
     * @param prestataire the Prestataire entity to convert
     * @return PrestaireDTO with relevant fields, or null if input is null
     */
    public PrestaireDTO toDTO(Prestataire prestataire) {
        if (prestataire == null) {
            return null;
        }

        return PrestaireDTO.builder()
            .id(prestataire.getId())
            .name(prestataire.getName())
            .description(prestataire.getDescription())
            .phone(prestataire.getPhone())
            .email(prestataire.getEmail())
            .address(prestataire.getAddress())
            .latitude(prestataire.getLatitude())
            .longitude(prestataire.getLongitude())
            .build();
    }

    /**
     * Convert PrestaireDTO to Prestataire entity.
     * Used for creating or updating prestataires from API requests.
     * Note: Sensitive fields like CIN and businessName must be handled separately.
     * 
     * @param dto the PrestaireDTO with source data
     * @return Prestataire entity with populated fields, or null if input is null
     */
    public Prestataire toEntity(PrestaireDTO dto) {
        if (dto == null) {
            return null;
        }

        return Prestataire.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .phone(dto.getPhone())
            .email(dto.getEmail())
            .address(dto.getAddress())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())

            .build();
    }

    /**
     * Update existing Prestataire entity with data from PrestaireDTO.
     * Preserves ID and other sensitive fields while updating public information.
     * 
     * @param dto the PrestaireDTO with updated data
     * @param prestataire the existing Prestataire entity to update
     * @return the updated Prestataire entity
     */
    public Prestataire updateEntity(PrestaireDTO dto, Prestataire prestataire) {
        if (dto == null) {
            return prestataire;
        }

        if (dto.getName() != null) {
            prestataire.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            prestataire.setDescription(dto.getDescription());
        }
        if (dto.getPhone() != null) {
            prestataire.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            prestataire.setEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            prestataire.setAddress(dto.getAddress());
        }
        if (dto.getLatitude() != null) {
            prestataire.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            prestataire.setLongitude(dto.getLongitude());
        }

        return prestataire;
    }
}
