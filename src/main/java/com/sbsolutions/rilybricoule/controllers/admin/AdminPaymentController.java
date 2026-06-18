package com.sbsolutions.rilybricoule.controllers.admin;


import com.sbsolutions.rilybricoule.dto.admin.AdminPaymentDTO;
import com.sbsolutions.rilybricoule.services.AdminPaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('PAYMENTS_VIEW')")
    public List<AdminPaymentDTO> getAll(@RequestParam(required = false) Long reservationId) {
        return service.getAll(reservationId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PAYMENTS_MANAGE')")
    public AdminPaymentDTO updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request
    ) {
        return service.updatePaymentStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/payout-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PAYMENTS_MANAGE')")
    public AdminPaymentDTO updatePayoutStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request
    ) {
        return service.updatePayoutStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/commission-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('PAYMENTS_MANAGE')")
    public AdminPaymentDTO updateCommissionStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request
    ) {
        return service.updateCommissionStatus(id, request.getStatus());
    }

    @Data
    public static class StatusRequest {
        private String status;
    }
}
