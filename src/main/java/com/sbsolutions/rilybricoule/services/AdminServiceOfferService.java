package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminServiceOfferDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Service;
import com.sbsolutions.rilybricoule.repository.ServiceRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminServiceOfferService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<AdminServiceOfferDTO> getAll() {
        return serviceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminServiceOfferDTO approve(Long id) {
        Service service = findService(id);

        service.setModerationStatus(Service.ModerationStatus.APPROVED);
        service.setModerationNote(null);

        return toDto(serviceRepository.save(service));
    }

    @Transactional
    public AdminServiceOfferDTO hide(Long id, String note) {
        Service service = findService(id);

        service.setModerationStatus(Service.ModerationStatus.HIDDEN);
        service.setModerationNote(cleanNote(note));

        return toDto(serviceRepository.save(service));
    }

    @Transactional
    public AdminServiceOfferDTO requestAdjustment(Long id, String note) {
        Service service = findService(id);

        service.setModerationStatus(Service.ModerationStatus.ADJUSTMENT_REQUIRED);
        service.setModerationNote(cleanNote(note));

        return toDto(serviceRepository.save(service));
    }

    private Service findService(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable avec ID " + id));
    }

    private String cleanNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }

        return note.trim();
    }

    private AdminServiceOfferDTO toDto(Service service) {
        Prestataire prestataire = service.getPrestataire();

        return AdminServiceOfferDTO.builder()
                .id(service.getId())
                .title(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .category(service.getCategory())
                .imageUrl(service.getImageUrl())
                .active(service.getModerationStatus() == Service.ModerationStatus.APPROVED)
                .moderationStatus(service.getModerationStatus().name())
                .moderationNote(service.getModerationNote())
                .providerId(prestataire != null ? prestataire.getId() : null)
                .providerName(buildProviderName(prestataire))
                .providerEmail(prestataire != null ? prestataire.getEmail() : null)
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    private String buildProviderName(Prestataire prestataire) {
        if (prestataire == null) {
            return "Prestataire";
        }

        if (prestataire.getBusinessName() != null && !prestataire.getBusinessName().isBlank()) {
            return prestataire.getBusinessName();
        }

        if (prestataire.getName() != null && !prestataire.getName().isBlank()) {
            return prestataire.getName();
        }

        String firstName = prestataire.getFirstName() != null ? prestataire.getFirstName() : "";
        String lastName = prestataire.getLastName() != null ? prestataire.getLastName() : "";

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? "Prestataire" : fullName;
    }

    @Data
    public static class NoteRequest {
        private String note;
    }
}