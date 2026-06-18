package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.NotificationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationCampaignRepository extends JpaRepository<NotificationCampaign, Long> {
    List<NotificationCampaign> findAllByOrderByCreatedAtDesc();
}