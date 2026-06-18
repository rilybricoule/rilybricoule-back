package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminMarketingSummaryDTO;
import com.sbsolutions.rilybricoule.services.AdminMarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/marketing")
@RequiredArgsConstructor
public class AdminMarketingController {

    private final AdminMarketingService adminMarketingService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('PROMOS_VIEW')")
    public AdminMarketingSummaryDTO getSummary() {
        return adminMarketingService.getSummary();
    }
}