package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByPrestataire_IdOrderByDateDesc(Long prestataireId);


}
