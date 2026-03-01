package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.NotificationPreferenceDto;
import com.sbsolutions.rilybricoule.entity.NotificationType;

public interface InotificationPreferenceService {

    // retourne les prefs du user; crée des valeurs par défaut si absentes
    NotificationPreferenceDto getOrCreate(Long userId);

    // met à jour partiellement les prefs (null = ne pas modifier)
    NotificationPreferenceDto update(Long userId, NotificationPreferenceDto dto);

    // utilisé par NotificationService avant envoi
    boolean canSend(Long userId, NotificationType type);
}