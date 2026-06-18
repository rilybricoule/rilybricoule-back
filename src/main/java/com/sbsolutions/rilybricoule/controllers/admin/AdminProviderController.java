package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminProviderDTO;
import com.sbsolutions.rilybricoule.services.AdminProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prestataires")
@RequiredArgsConstructor
public class AdminProviderController {

    private final AdminProviderService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('PROVIDERS_VIEW')")
    public List<AdminProviderDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('PROVIDERS_VIEW')")
    public AdminProviderDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_APPROVE')")
    public AdminProviderDTO approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_APPROVE')")
    public AdminProviderDTO reject(@PathVariable Long id) {
        return service.reject(id);
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_SUSPEND')")
    public AdminProviderDTO suspend(@PathVariable Long id) {
        return service.suspend(id);
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PROVIDERS_SUSPEND')")
    public AdminProviderDTO reactivate(@PathVariable Long id) {
        return service.reactivate(id);
    }
}
