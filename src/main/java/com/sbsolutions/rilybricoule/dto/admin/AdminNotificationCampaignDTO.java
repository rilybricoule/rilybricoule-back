package com.sbsolutions.rilybricoule.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNotificationCampaignDTO {
    private Long id;
    private String title;
    private String message;
    private String audience;
    private String status;
    private String notificationType;
    private Long targetUserId;
    private String targetEmail;
    private Integer recipientCount;
    private Integer readCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean sendNow;
}