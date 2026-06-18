package com.sbsolutions.rilybricoule.dto.tickets;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSupportTicketDTO {

    private Long id;
    private String subject;
    private String category;
    private String status;
    private boolean litige;
    private String fromType;
    private String fromId;
    private String fromName;
    private String reservationId;
    private String reservationTitle;
    private String adminNote;
    private String resolutionNote;
    private String resolutionAction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageDTO> messages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDTO {
        private String senderType;
        private String senderName;
        private String content;
        private LocalDateTime createdAt;
    }
}
