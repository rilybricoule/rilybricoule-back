package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminNotificationCampaignDTO;
import com.sbsolutions.rilybricoule.services.AdminNotificationCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notification-campaigns")
@RequiredArgsConstructor
public class AdminNotificationCampaignController {

    private final AdminNotificationCampaignService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MODERATEUR') or hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public List<AdminNotificationCampaignDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MODERATEUR') or hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public AdminNotificationCampaignDTO create(@RequestBody AdminNotificationCampaignDTO request) {
        return service.create(request);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MODERATEUR') or hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public AdminNotificationCampaignDTO send(@PathVariable Long id) {
        return service.send(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MODERATEUR') or hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public AdminNotificationCampaignDTO cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}