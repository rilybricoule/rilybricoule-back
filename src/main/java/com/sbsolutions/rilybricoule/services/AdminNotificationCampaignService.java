package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminNotificationCampaignDTO;
import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.entity.NotificationCampaign;
import com.sbsolutions.rilybricoule.entity.NotificationType;
import com.sbsolutions.rilybricoule.entity.RoleName;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.NotificationCampaignRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationCampaignService {

    private final NotificationCampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final INotificationService notificationService;

    @Transactional(readOnly = true)
    public List<AdminNotificationCampaignDTO> getAll() {
        return campaignRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminNotificationCampaignDTO create(AdminNotificationCampaignDTO request) {

        Long targetUserId = request.getTargetUserId();

        if ("SPECIFIC_USER".equals(request.getAudience())
                && targetUserId == null
                && request.getTargetEmail() != null
                && !request.getTargetEmail().isBlank()) {

            User target = userRepository.findByEmail(request.getTargetEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec cet email"));

            targetUserId = target.getId();
        }

        NotificationCampaign campaign = NotificationCampaign.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .audience(request.getAudience())
                .notificationType(request.getNotificationType())
                .targetUserId(targetUserId)
                .scheduledAt(request.getScheduledAt())
                .status(resolveInitialStatus(request))
                .recipientCount(0)
                .readCount(0)
                .build();

        NotificationCampaign saved = campaignRepository.save(campaign);

        if (Boolean.TRUE.equals(request.getSendNow())) {
            return send(saved.getId());
        }

        return toDto(saved);
    }

    @Transactional
    public AdminNotificationCampaignDTO send(Long id) {
        NotificationCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if ("CANCELLED".equals(campaign.getStatus())) {
            throw new IllegalStateException("Cannot send a cancelled campaign");
        }

        List<User> recipients = resolveRecipients(campaign);

        for (User user : recipients) {
            NotificationInputDto input = new NotificationInputDto();
            input.setReceiverId(user.getId());
            input.setContenu(campaign.getMessage());
            input.setType(resolveNotificationType(campaign.getNotificationType()));
            input.setDate(LocalDateTime.now());

            notificationService.createNotification(input);
        }

        campaign.setStatus("SENT");
        campaign.setSentAt(LocalDateTime.now());
        campaign.setRecipientCount(recipients.size());
        campaign.setFailureReason(null);

        NotificationCampaign saved = campaignRepository.save(campaign);

        return toDto(saved);
    }

    @Transactional
    public AdminNotificationCampaignDTO cancel(Long id) {
        NotificationCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if ("SENT".equals(campaign.getStatus())) {
            throw new IllegalStateException("Cannot cancel an already sent campaign");
        }

        campaign.setStatus("CANCELLED");

        return toDto(campaignRepository.save(campaign));
    }

    private String resolveInitialStatus(AdminNotificationCampaignDTO request) {
        if (Boolean.TRUE.equals(request.getSendNow())) {
            return "SENT";
        }

        if (request.getScheduledAt() != null) {
            return "SCHEDULED";
        }

        return "DRAFT";
    }

    private List<User> resolveRecipients(NotificationCampaign campaign) {
        String audience = campaign.getAudience();

        if ("SPECIFIC_USER".equals(audience)) {
            if (campaign.getTargetUserId() == null) {
                throw new IllegalArgumentException("Target user is required");
            }

            User user = userRepository.findById(campaign.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            return List.of(user);
        }

        if ("ALL_PROVIDERS".equals(audience)) {
            return userRepository.findByRoles_RoleName(RoleName.ROLE_PRESTATAIRE);
        }

        if ("ALL_CLIENTS".equals(audience)) {
            return userRepository.findByRoles_RoleName(RoleName.ROLE_CLIENT);
        }

        return List.of();
    }

    private NotificationType resolveNotificationType(String value) {
        if (value == null || value.isBlank()) {
            return NotificationType.MARKETING;
        }

        try {
            return NotificationType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return NotificationType.MARKETING;
        }
    }

    private AdminNotificationCampaignDTO toDto(NotificationCampaign campaign) {
        AdminNotificationCampaignDTO dto = new AdminNotificationCampaignDTO();

        dto.setId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setMessage(campaign.getMessage());
        dto.setAudience(campaign.getAudience());
        dto.setStatus(campaign.getStatus());
        dto.setNotificationType(campaign.getNotificationType());
        dto.setTargetUserId(campaign.getTargetUserId());
        dto.setRecipientCount(campaign.getRecipientCount());
        dto.setReadCount(campaign.getReadCount());
        dto.setScheduledAt(campaign.getScheduledAt());
        dto.setSentAt(campaign.getSentAt());
        dto.setFailureReason(campaign.getFailureReason());
        dto.setCreatedAt(campaign.getCreatedAt());
        dto.setUpdatedAt(campaign.getUpdatedAt());

        return dto;
    }
}
