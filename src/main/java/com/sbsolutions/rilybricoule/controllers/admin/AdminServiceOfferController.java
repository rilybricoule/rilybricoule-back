package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminServiceOfferDTO;
import com.sbsolutions.rilybricoule.services.AdminServiceOfferService;
import com.sbsolutions.rilybricoule.services.AdminServiceOfferService.NoteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
public class AdminServiceOfferController {

    private final AdminServiceOfferService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('SERVICES_VIEW')")
    public List<AdminServiceOfferDTO> getAll() {
        return service.getAll();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('SERVICES_MANAGE')")
    public AdminServiceOfferDTO approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('SERVICES_MANAGE')")
    public AdminServiceOfferDTO hide(
            @PathVariable Long id,
            @RequestBody(required = false) NoteRequest request
    ) {
        return service.hide(id, request != null ? request.getNote() : null);
    }

    @PatchMapping("/{id}/request-adjustment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('SERVICES_MANAGE')")
    public AdminServiceOfferDTO requestAdjustment(
            @PathVariable Long id,
            @RequestBody(required = false) NoteRequest request
    ) {
        return service.requestAdjustment(id, request != null ? request.getNote() : null);
    }
}
