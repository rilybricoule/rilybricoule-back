package com.sbsolutions.rilybricoule.service.repository;

import com.sbsolutions.rilybricoule.service.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}
