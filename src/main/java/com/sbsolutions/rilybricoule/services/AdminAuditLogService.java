package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AuditLogDTO;
import com.sbsolutions.rilybricoule.security.adapter.out.persistence.AuditLogJpaRepository;
import com.sbsolutions.rilybricoule.security.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AuditLogJpaRepository auditLogRepository;

    public Page<AuditLogDTO> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return auditLogRepository.findAll(pageable)
                .map(this::toDto);
    }

    private AuditLogDTO toDto(AuditLog log) {
        return new AuditLogDTO(
                log.getId(),
                null,
                log.getEmail() != null ? log.getEmail() : "Systeme",
                log.getEmail(),
                log.getAction(),
                "Securite",
                log.getDetails(),
                log.getTimestamp(),
                log.getIpAddress()
        );
    }
}