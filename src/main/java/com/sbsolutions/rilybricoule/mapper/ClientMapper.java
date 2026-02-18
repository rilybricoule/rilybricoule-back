package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.ClientDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Client entity and ClientDTO.
 * Ensures clean separation between database entities and API request/response objects.
 * Excludes sensitive fields not needed in API responses.
 */
@Component
public class ClientMapper {

    /**
     * Convert Client entity to ClientDTO.
     * Extracts only non-sensitive client information for API responses.
     * 
     * @param client the Client entity to convert
     * @return ClientDTO with relevant fields, or null if input is null
     */
    public ClientDTO toDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientDTO.builder()
            .id(client.getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .phone(client.getPhone())
            .address(client.getAddress())
            .latitude(client.getLatitude())
            .longitude(client.getLongitude())
            .build();
    }

    /**
     * Convert ClientDTO to Client entity.
     * Used for creating or updating clients from API requests.
     * Note: Password and role information is not handled here (must be managed separately).
     * 
     * @param dto the ClientDTO with source data
     * @return Client entity with populated fields, or null if input is null
     */
    public Client toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }

        return Client.builder()
            .id(dto.getId())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .address(dto.getAddress())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .build();
    }

    /**
     * Update existing Client entity with data from ClientDTO.
     * Preserves ID and other sensitive fields while updating contact information.
     * 
     * @param dto the ClientDTO with updated data
     * @param client the existing Client entity to update
     * @return the updated Client entity
     */
    public Client updateEntity(ClientDTO dto, Client client) {
        if (dto == null) {
            return client;
        }

        if (dto.getFirstName() != null) {
            client.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            client.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            client.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            client.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            client.setAddress(dto.getAddress());
        }
        if (dto.getLatitude() != null) {
            client.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            client.setLongitude(dto.getLongitude());
        }

        return client;
    }
}
