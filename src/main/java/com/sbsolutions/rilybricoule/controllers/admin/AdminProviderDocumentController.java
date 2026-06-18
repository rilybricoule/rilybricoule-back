package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.ProviderDocumentDTO;
import com.sbsolutions.rilybricoule.dto.UpdateProviderDocumentStatusRequest;
import com.sbsolutions.rilybricoule.services.ProviderDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/prestataires")
@RequiredArgsConstructor
public class AdminProviderDocumentController {

    private final ProviderDocumentService providerDocumentService;

    @GetMapping("/{providerId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MODERATEUR','SUPPORT') or hasAuthority('PROVIDERS_VIEW')")
    public List<ProviderDocumentDTO> getProviderDocuments(@PathVariable Long providerId) {
        return providerDocumentService.getProviderDocuments(providerId);
    }

    @PatchMapping("/documents/{documentId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_APPROVE')")
    public ProviderDocumentDTO updateDocumentStatus(
            @PathVariable Long documentId,
            @RequestBody UpdateProviderDocumentStatusRequest request,
            Authentication authentication
    ) {
        return providerDocumentService.updateDocumentStatus(documentId, request, authentication);
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_APPROVE')")
    public Map<String, String> deleteDocument(@PathVariable Long documentId) {
        providerDocumentService.deleteDocument(documentId);
        return Map.of("message", "Document deleted successfully");
    }
}