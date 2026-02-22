package com.sbsolutions.rilybricoule.security.adapter.out.persistence;

import com.sbsolutions.rilybricoule.security.domain.model.AuditLog;
import com.sbsolutions.rilybricoule.security.domain.port.out.AuditLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogPersistenceAdapter implements AuditLogPort {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginSuccess(String email, String ipAddress, String userAgent) {
        log.info("LOGIN_SUCCESS | email={} | ip={}", email, ipAddress);
        persist("LOGIN_SUCCESS", email, ipAddress, userAgent, true, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginFailure(String email, String ipAddress, String userAgent, String reason) {
        log.warn("LOGIN_FAILURE | email={} | ip={} | reason={}", email, ipAddress, reason);
        persist("LOGIN_FAILURE", email, ipAddress, userAgent, false, reason);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRegister(String email, String ipAddress, String userAgent) {
        log.info("REGISTER | email={} | ip={}", email, ipAddress);
        persist("REGISTER", email, ipAddress, userAgent, true, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTokenRefresh(String email, String ipAddress, String userAgent) {
        log.info("TOKEN_REFRESH | email={} | ip={}", email, ipAddress);
        persist("TOKEN_REFRESH", email, ipAddress, userAgent, true, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTokenRefreshFailure(String ipAddress, String userAgent, String reason) {
        log.warn("TOKEN_REFRESH_FAILURE | ip={} | reason={}", ipAddress, reason);
        persist("TOKEN_REFRESH_FAILURE", null, ipAddress, userAgent, false, reason);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogout(String email, String ipAddress, String userAgent) {
        log.info("LOGOUT | email={} | ip={}", email, ipAddress);
        persist("LOGOUT", email, ipAddress, userAgent, true, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInvalidTokenAttempt(String ipAddress, String userAgent, String reason) {
        log.warn("INVALID_TOKEN | ip={} | reason={}", ipAddress, reason);
        persist("INVALID_TOKEN", null, ipAddress, userAgent, false, reason);
    }

    private void persist(String action, String email, String ipAddress, String userAgent, boolean success, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .email(email)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogJpaRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }
}
