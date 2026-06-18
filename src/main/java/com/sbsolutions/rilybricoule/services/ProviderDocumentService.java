package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.ProviderDocumentDTO;
import com.sbsolutions.rilybricoule.dto.UpdateProviderDocumentStatusRequest;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.ProviderDocument;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ProviderDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProviderDocumentService {

    private final ProviderDocumentRepository providerDocumentRepository;
    private final PrestaireRepository prestaireRepository;

    public List<ProviderDocumentDTO> getProviderDocuments(Long providerId) {
        ensureProviderExists(providerId);

        return providerDocumentRepository.findByPrestataire_IdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ProviderDocumentDTO updateDocumentStatus(
            Long documentId,
            UpdateProviderDocumentStatusRequest request,
            Authentication authentication
    ) {
        ProviderDocument document = providerDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document introuvable avec ID " + documentId
                ));

        ProviderDocument.DocumentStatus status = parseDocumentStatus(request.getStatus());

        document.setStatus(status);
        document.setReviewNote(request.getReviewNote());
        document.setReviewedAt(LocalDateTime.now());

        Long adminId = extractUserId(authentication);
        document.setReviewedByAdminId(adminId);

        return toDTO(providerDocumentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        if (!providerDocumentRepository.existsById(documentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document introuvable avec ID " + documentId
            );
        }

        providerDocumentRepository.deleteById(documentId);
    }

    private void ensureProviderExists(Long providerId) {
        if (!prestaireRepository.existsById(providerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Prestataire introuvable avec ID " + providerId
            );
        }
    }

    private ProviderDocument.DocumentStatus parseDocumentStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        try {
            return ProviderDocument.DocumentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid document status. Use PENDING, APPROVED, or REJECTED"
            );
        }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        try {
            Object id = principal.getClass().getMethod("getId").invoke(principal);
            if (id instanceof Long value) {
                return value;
            }
        } catch (ReflectiveOperationException ignored) {
            // Spring Security principal may not expose id; keeping null is acceptable.
        }

        return null;
    }

    private ProviderDocumentDTO toDTO(ProviderDocument document) {
        Prestataire provider = document.getPrestataire();

        return ProviderDocumentDTO.builder()
                .id(document.getId())
                .prestataireId(provider.getId())
                .type(document.getType().name())
                .fileUrl(document.getFileUrl())
                .originalFileName(document.getOriginalFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .status(document.getStatus().name())
                .reviewNote(document.getReviewNote())
                .reviewedByAdminId(document.getReviewedByAdminId())
                .createdAt(document.getCreatedAt())
                .reviewedAt(document.getReviewedAt())
                .build();
    }
}