package com.sbsolutions.rilybricoule.dto.admin;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        Long adminId,
        String adminName,
        String adminEmail,
        String action,
        String module,
        String details,
        LocalDateTime createdAt,
        String ipAddress
) {
}