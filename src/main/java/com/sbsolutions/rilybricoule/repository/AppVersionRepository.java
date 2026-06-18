package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    List<AppVersion> findAllByOrderByReleaseDateDesc();
}