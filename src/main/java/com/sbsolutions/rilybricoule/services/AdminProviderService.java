package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminProviderDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.repository.AvisRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminProviderService {

    private final PrestaireRepository prestaireRepository;
    private final ReservationRepository reservationRepository;
    private final AvisRepository avisRepository;

    @Transactional(readOnly = true)
    public List<AdminProviderDTO> getAll() {
        return prestaireRepository.findAll()
                .stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(
                        AdminProviderDTO::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProviderDTO getById(Long id) {
        return toDto(findProvider(id));
    }

    @Transactional
    public AdminProviderDTO approve(Long id) {
        Prestataire provider = findProvider(id);

        provider.setStatus(Prestataire.ProviderStatus.APPROVED);
        provider.setVerified(true);
        provider.setAvailable(true);
        provider.setEnabled(true);
        provider.setAdminComment(null);

        return toDto(prestaireRepository.save(provider));
    }

    @Transactional
    public AdminProviderDTO reject(Long id) {
        Prestataire provider = findProvider(id);

        provider.setStatus(Prestataire.ProviderStatus.REJECTED);
        provider.setVerified(false);
        provider.setAvailable(false);
        provider.setEnabled(false);

        return toDto(prestaireRepository.save(provider));
    }

    @Transactional
    public AdminProviderDTO suspend(Long id) {
        Prestataire provider = findProvider(id);

        provider.setStatus(Prestataire.ProviderStatus.SUSPENDED);
        provider.setAvailable(false);
        provider.setEnabled(false);

        return toDto(prestaireRepository.save(provider));
    }

    @Transactional
    public AdminProviderDTO reactivate(Long id) {
        Prestataire provider = findProvider(id);

        provider.setStatus(Prestataire.ProviderStatus.APPROVED);
        provider.setVerified(true);
        provider.setAvailable(true);
        provider.setEnabled(true);

        return toDto(prestaireRepository.save(provider));
    }

    private Prestataire findProvider(Long id) {
        return prestaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestataire introuvable avec ID " + id));
    }

    private AdminProviderDTO toDto(Prestataire provider) {
        long completedInterventions =
                reservationRepository.countByPrestataire_IdAndStatus(
                        provider.getId(),
                        Reservation.ReservationStatus.COMPLETED
                );

        Double averageRating =
                avisRepository.findAverageRatingByPrestataireId(provider.getId());

        Prestataire.ProviderStatus status = resolveStatus(provider);

        return AdminProviderDTO.builder()
                .id(provider.getId())
                .email(provider.getEmail())
                .firstName(provider.getFirstName())
                .lastName(provider.getLastName())
                .phone(provider.getPhone())
                .city(extractCity(provider.getAddress()))
                .name(provider.getName())
                .businessName(provider.getBusinessName())
                .description(provider.getDescription())
                .address(provider.getAddress())
                .enabled(provider.isEnabled())
                .status(status.name())
                .verified(provider.isVerified())
                .available(provider.isAvailable())
                .active(provider.isEnabled())
                .adminComment(provider.getAdminComment())
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .completedInterventions(completedInterventions)
                .averageRating(averageRating == null ? 0.0 : averageRating)
                .build();
    }

    private Prestataire.ProviderStatus resolveStatus(Prestataire provider) {
        if (provider.getStatus() != null) {
            return provider.getStatus();
        }

        if (!provider.isEnabled() || !provider.isAvailable()) {
            return Prestataire.ProviderStatus.SUSPENDED;
        }

        if (provider.isVerified()) {
            return Prestataire.ProviderStatus.APPROVED;
        }

        return Prestataire.ProviderStatus.PENDING;
    }

    private String extractCity(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        return address.split(",")[0].trim();
    }
}
