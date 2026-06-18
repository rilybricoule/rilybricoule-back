package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.NotificationPreferenceDto;
import com.sbsolutions.rilybricoule.entity.NotificationPreference;
import com.sbsolutions.rilybricoule.entity.NotificationType;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.NotificationPreferenceRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService implements InotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationPreferenceDto getOrCreate(Long userId) {
        NotificationPreference pref = getOrCreateEntity(userId);
        return toDto(pref);
    }

    @Override
    @Transactional
    public NotificationPreferenceDto update(Long userId, NotificationPreferenceDto dto) {
        NotificationPreference pref = getOrCreateEntity(userId);

        // partial update: null = keep current value
        if (dto.getEnabled() != null) pref.setEnabled(dto.getEnabled());
        if (dto.getMessageEnabled() != null) pref.setMessageEnabled(dto.getMessageEnabled());
        if (dto.getReservationEnabled() != null) pref.setReservationEnabled(dto.getReservationEnabled());
        if (dto.getPaiementEnabled() != null) pref.setPaiementEnabled(dto.getPaiementEnabled());
        if (dto.getAvisEnabled() != null) pref.setAvisEnabled(dto.getAvisEnabled());

        NotificationPreference saved = preferenceRepository.save(pref);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSend(Long userId, NotificationType type) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId).orElse(null);

        // If no preferences yet, allow by default
        if (pref == null) return true;

        // If notifications are globally disabled, block
        if (!pref.isEnabled()) return false;

        return switch (type) {
            case MESSAGE -> pref.isMessageEnabled();
            case RESERVATION -> pref.isReservationEnabled();
            case PAIEMENT -> pref.isPaiementEnabled();
            case AVIS -> pref.isAvisEnabled();

            // These are system/admin-level notifications.
            // If global notifications are enabled, allow them by default.
            case WARNING, MARKETING, ACCOUNT, DISPUTE, SUPPORT, SYSTEM, PROMO -> true;
        };
    }
    private NotificationPreference getOrCreateEntity(Long userId) {
        return preferenceRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

            NotificationPreference created = NotificationPreference.builder()
                    .user(user)
                    .enabled(true)
                    .messageEnabled(true)
                    .reservationEnabled(true)
                    .paiementEnabled(true)
                    .avisEnabled(true)
                    .build();

            return preferenceRepository.save(created);
        });
    }

    private NotificationPreferenceDto toDto(NotificationPreference pref) {
        return NotificationPreferenceDto.builder()
                .userId(pref.getUser().getId())
                .enabled(pref.isEnabled())
                .messageEnabled(pref.isMessageEnabled())
                .reservationEnabled(pref.isReservationEnabled())
                .paiementEnabled(pref.isPaiementEnabled())
                .avisEnabled(pref.isAvisEnabled())
                .build();
    }
}
