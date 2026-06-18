package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.*;
import com.sbsolutions.rilybricoule.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService service;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardOverviewDTO getOverview() {
        return service.getOverview();
    }

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardPerformanceDTO getPerformance() {
        return service.getPerformance();
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardTrendsDTO getTrends() {
        return service.getTrends();
    }

    @GetMapping("/extra-kpis")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardExtraKpiDTO getExtraKpis() {
        return service.getExtraKpis();
    }

    @GetMapping("/quality-trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardQualityTrendsDTO getQualityTrends() {
        return service.getQualityTrends();
    }

    @GetMapping("/payment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('DASHBOARD_VIEW')")
    public DashboardPaymentStatusDTO getPaymentStatus() {
        return service.getPaymentStatus();
    }
}
