package com.sbsolutions.rilybricoule.dto.input;

import com.sbsolutions.rilybricoule.entity.notificationType.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInputDto {
    private String contenu;
    private NotificationType type;      // optional if backend decides it
    private Long prestataireId;         // destinataire
    private LocalDateTime date;         // optionnel : date d'envoi
}

