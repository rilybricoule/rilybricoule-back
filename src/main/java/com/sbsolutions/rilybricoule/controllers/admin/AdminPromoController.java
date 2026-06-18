package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminPromoDTO;
import com.sbsolutions.rilybricoule.services.AdminPromoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promos")
@RequiredArgsConstructor
public class AdminPromoController {

    private final AdminPromoService adminPromoService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROMOS_VIEW')")
    public List<AdminPromoDTO> getAll() {
        return adminPromoService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOS_MANAGE')")
    public AdminPromoDTO create(@RequestBody AdminPromoDTO request) {
        return adminPromoService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOS_MANAGE')")
    public AdminPromoDTO update(
            @PathVariable Long id,
            @RequestBody AdminPromoDTO request
    ) {
        return adminPromoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOS_MANAGE')")
    public void delete(@PathVariable Long id) {
        adminPromoService.delete(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PROMOS_MANAGE')")
    public AdminPromoDTO updateStatus(
            @PathVariable Long id,
            @RequestBody UpdatePromoStatusRequest request
    ) {
        return adminPromoService.updateStatus(id, request.isActive());
    }

    @Data
    public static class UpdatePromoStatusRequest {
        private boolean active;
    }
}