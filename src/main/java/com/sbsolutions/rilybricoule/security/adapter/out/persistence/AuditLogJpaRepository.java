package com.sbsolutions.rilybricoule.security.adapter.out.persistence;

import com.sbsolutions.rilybricoule.security.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
}
