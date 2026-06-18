package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminSettingsDTO;
import com.sbsolutions.rilybricoule.services.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('SETTINGS_VIEW')")
    public AdminSettingsDTO get() {
        return service.get();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('SETTINGS_EDIT')")
    public AdminSettingsDTO update(@RequestBody AdminSettingsDTO request) {
        return service.update(request);
    }
}
