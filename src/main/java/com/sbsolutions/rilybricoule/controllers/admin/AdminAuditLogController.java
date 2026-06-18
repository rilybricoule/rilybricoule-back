package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AuditLogDTO;
import com.sbsolutions.rilybricoule.services.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW_ALL') or hasAuthority('AUDIT_VIEW_OWN')")
    public Page<AuditLogDTO> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return adminAuditLogService.getAuditLogs(page, size);
    }
}